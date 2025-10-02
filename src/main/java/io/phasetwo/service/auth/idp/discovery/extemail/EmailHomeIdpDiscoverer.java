package io.phasetwo.service.auth.idp.discovery.extemail;

import com.google.common.base.Strings;
import io.phasetwo.service.auth.idp.PublicAPI;
import io.phasetwo.service.auth.idp.Users;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.phasetwo.service.Orgs.ORG_CONFIG_VALIDATE_IDP_KEY;
import static io.phasetwo.service.Orgs.ORG_DOMAIN_CONFIG_KEY;
import static io.phasetwo.service.Orgs.ORG_VALIDATION_PENDING_CONFIG_KEY;

@PublicAPI(unstable = true)
public final class EmailHomeIdpDiscoverer implements HomeIdpDiscoverer {

    private static final Logger LOG = Logger.getLogger(EmailHomeIdpDiscoverer.class);
    private static final String EMAIL_ATTRIBUTE = "email";
    private final Users users;
    private final IdentityProviders identityProviders;

    @PublicAPI(unstable = true)
    public EmailHomeIdpDiscoverer(Users users, IdentityProviders identityProviders) {
        this.users = users;
        this.identityProviders = identityProviders;
    }

    @Override
    public List<IdentityProviderModel> discoverForUser(AuthenticationFlowContext context, String username) {

        String realmName = context.getRealm().getName();
        EmailHomeIdpDiscovererConfig config = new EmailHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
        LOG.tracef("Trying to discover home IdP for username '%s' in realm '%s' with authenticator config '%s'",
                username, realmName, config == null ? "<unconfigured>" : config.getAlias());

        DomainExtractor domainExtractor = new DomainExtractor(config);
        LOG.tracef("Trying to discover home IdP for username '%s' in realm '%s' with authenticator config '%s'",
                username, realmName, config.getAlias());

        List<IdentityProviderModel> homeIdps = new ArrayList<>();

        final Optional<Domain> emailDomain;
        UserModel user = users.lookupBy(username);
        if (user == null) {
            LOG.tracef("No user found in AuthenticationFlowContext. Extracting domain from provided username '%s'.",
                    username);
            emailDomain = domainExtractor.extractFrom(username);
        } else {
            LOG.tracef("User found in AuthenticationFlowContext. Extracting domain from stored user '%s'.",
                    user.getId());
            if (EMAIL_ATTRIBUTE.equalsIgnoreCase(config.userAttribute())
                    && !user.isEmailVerified()
                    && config.requireVerifiedEmail()) {
                LOG.warnf("Email address of user '%s' is not verified and forwarding not enabled", user.getId());
                emailDomain = Optional.empty();
            } else {
                emailDomain = domainExtractor.extractFrom(user);
            }
        }

        if (emailDomain.isPresent()) {
            Domain domain = emailDomain.get();
            homeIdps = discoverHomeIdps(context, domain, user, username);
            if (homeIdps.isEmpty()) {
                LOG.debugf("Could not find home IdP for domain '%s' and user '%s' in realm '%s'",
                        domain, username, realmName);
            } else {
                String homeIdpsString = homeIdps.stream()
                        .map(IdentityProviderModel::getAlias)
                        .collect(Collectors.joining(","));
                LOG.infof("Found IdPs [%s] with domain '%s' for user '%s'", homeIdpsString, domain, username);
            }
        } else {
            LOG.warnf("Could not extract domain from email address '%s'", username);
        }

        return homeIdps;
    }

    /**
     * Get a list of idps given an email domain, user and username.
     * 1. If the user is set in the context, initially look up the set of federated identities that match the user.
     * 2. Look up all enabled idps for the realm. This seems unneccessary and a performance risk.
     * 3. Get a stream of organizations with a matching email domain. Map those to idps.
     * 3a. If multi-idps is turned on, get a subset of that list with domain matches in the config.
     * 4. Get a subset of the list that match the user's federated identities. Return that if it's non-empty.
     * 5. If empty, but user has linked idps, prefer linked and enabled IdPs without matching domain in favor of not linked IdPs with matching domain
     * 6. If empty, but user doesn't have linked idps, fallback to not linked IdPs with matching domain (general case if user logs in for the first time)
     * @param domain Email domain
     * @param user User if set in the context
     * @param username Username or email
     * @returns A list of Identity Providers
     */
    private List<IdentityProviderModel> discoverHomeIdps(AuthenticationFlowContext context, Domain domain, UserModel user, String username) {
        final Map<String, String> linkedIdps;

        EmailHomeIdpDiscovererConfig config = new EmailHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
        if (user == null || !config.forwardToLinkedIdp()) {
            linkedIdps = Collections.emptyMap();
            LOG.tracef(
                    "User '%s' is not stored locally or forwarding to linked IdP is disabled. Skipping discovery of linked IdPs.",
                    username);
        } else {
            LOG.tracef(
                    "Found local user '%s' and forwarding to linked IdP is enabled. Discovering linked IdPs.",
                    username);
            linkedIdps = context
                    .getSession()
                    .users()
                    .getFederatedIdentitiesStream(context.getRealm(), user)
                    .collect(
                            Collectors.toMap(FederatedIdentityModel::getIdentityProvider, FederatedIdentityModel::getUserName));
        }

        List<IdentityProviderModel> enabledIdps = determineEnabledIdps(context);
        // Original; lookup mechanism from https://github.com/sventorben/keycloak-home-idp-discovery
        /*
        List<IdentityProviderModel> enabledIdpsWithMatchingDomain = filterIdpsWithMatchingDomainFrom(enabledIdps,
            domain,
            config);
        */
        // Overidden lookup mechanism to lookup via organization domain
        OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
        boolean validateIdpEnabled = context.getRealm().getAttribute(ORG_CONFIG_VALIDATE_IDP_KEY, false);
        List<IdentityProviderModel> enabledIdpsWithMatchingDomain =
                orgs.getOrganizationsStreamForDomain(
                                context.getRealm(), domain.toString(), config.requireVerifiedDomain())
                        .flatMap(OrganizationModel::getIdentityProvidersStream)
                        .filter(IdentityProviderModel::isEnabled)
                        .filter(
                                idp ->
                                        !validateIdpEnabled
                                                || !isIdpValidationPending(idp))
                        .collect(Collectors.toList());

        // If multi-idps is turned on, get a subset of that list with domain matches in the config.
        if (io.phasetwo.service.util.IdentityProviders.isMultipleIdpsConfigEnabled(context.getRealm())) {
            List<IdentityProviderModel> domainMatchingIdps =
                    enabledIdpsWithMatchingDomain
                            .stream()
                            .filter(idp -> {
                                String domains = idp.getConfig().get(ORG_DOMAIN_CONFIG_KEY);
                                if (Strings.isNullOrEmpty(domains)) return false;
                                return io.phasetwo.service.util.IdentityProviders.strListContains(domains, domain.toString());
                            })
                            .distinct()
                            .collect(Collectors.toList());
            // If there are _any_ matches, use that list. If there are none, use the original list.
            if (!domainMatchingIdps.isEmpty()) {
                enabledIdpsWithMatchingDomain = domainMatchingIdps;
            }
        }

        // Prefer linked IdP with matching domain first
        List<IdentityProviderModel> homeIdps = getLinkedIdpsFrom(enabledIdpsWithMatchingDomain, linkedIdps);

        if (homeIdps.isEmpty()) {
            if (!linkedIdps.isEmpty()) {
                // Prefer linked and enabled IdPs without matching domain in favor of not linked IdPs with matching domain
                homeIdps = getLinkedIdpsFrom(enabledIdps, linkedIdps);
            }
            if (homeIdps.isEmpty()) {
                // Fallback to not linked IdPs with matching domain (general case if user logs in for the first time)
                homeIdps = enabledIdpsWithMatchingDomain;
                logFoundIdps("non-linked", "matching", homeIdps, domain, username);
            } else {
                logFoundIdps("non-linked", "non-matching", homeIdps, domain, username);
            }
        } else {
            logFoundIdps("linked", "matching", homeIdps, domain, username);
        }

        return homeIdps;
    }

    private void logFoundIdps(String idpQualifier, String domainQualifier, List<IdentityProviderModel> homeIdps, Domain domain, String username) {
        String homeIdpsString = homeIdps.stream()
                .map(IdentityProviderModel::getAlias)
                .collect(Collectors.joining(","));
        LOG.tracef("Found %s IdPs [%s] with %s domain '%s' for user '%s'",
                idpQualifier, homeIdpsString, domainQualifier, domain, username);
    }

    /**
     * Given a list of idps and a map of idp alias to federated username, return a subset of the list that are contained in the map keys.
     * @param enabledIdpsWithMatchingDomain A list of identity providers
     * @param linkedIdps A map of idp alias to federated username
     * @returns A subset of the list that are contained in the map keys
     */
    private List<IdentityProviderModel> getLinkedIdpsFrom(List<IdentityProviderModel> enabledIdpsWithMatchingDomain, Map<String, String> linkedIdps) {
        return enabledIdpsWithMatchingDomain.stream()
                .filter(it -> linkedIdps.containsKey(it.getAlias()))
                .collect(Collectors.toList());
    }

    private boolean isIdpValidationPending(IdentityProviderModel idp) {
        return Boolean.parseBoolean(
                Optional.ofNullable(idp.getConfig())
                        .map(cfg -> cfg.get(ORG_VALIDATION_PENDING_CONFIG_KEY))
                        .orElse(null));
    }

//
//    private List<IdentityProviderModel> filterIdpsWithMatchingDomainFrom(List<IdentityProviderModel> enabledIdps, Domain domain, HomeIdpDiscoveryConfig config) {
//        String userAttributeName = config.userAttribute();
//        List<IdentityProviderModel> idpsWithMatchingDomain = enabledIdps.stream()
//                .filter(it -> new io.phasetwo.service.auth.idp.IdentityProviderModelConfig(it).supportsDomain(userAttributeName, domain))
//                .collect(Collectors.toList());
//        LOG.tracef("IdPs with matching domain '%s' for attribute '%s': %s", domain, userAttributeName,
//                idpsWithMatchingDomain.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
//        return idpsWithMatchingDomain;
//    }

    /**
     * @returns A list of all enabled idps for a realm.
     */
    private List<IdentityProviderModel> determineEnabledIdps(AuthenticationFlowContext context) {
        RealmModel realm = context.getRealm();
        List<IdentityProviderModel> enabledIdps = context.getSession().identityProviders().getAllStream()
                .filter(IdentityProviderModel::isEnabled)
                .collect(Collectors.toList());
        LOG.tracef("Enabled IdPs in realm '%s': %s",
                realm.getName(),
                enabledIdps.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return enabledIdps;
    }

    @Override
    public void close() {
    }
}
