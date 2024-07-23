//package io.phasetwo.service.auth.idp.discovery.email;
package io.phasetwo.service.auth.idp.discovery.email;

import io.phasetwo.service.auth.idp.PublicAPI;
import io.phasetwo.service.auth.idp.Users;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

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
        EmailHomeIdpDiscovererConfig config = new EmailHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
        DomainExtractor domainExtractor = new DomainExtractor(config);

        String realmName = context.getRealm().getName();
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
            if (EMAIL_ATTRIBUTE.equalsIgnoreCase(config.userAttribute()) && !user.isEmailVerified()
                && !config.forwardUserWithUnverifiedEmail()) {
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
                LOG.infof("Could not find home IdP for domain '%s' and user '%s' in realm '%s'",
                    domain, username, realmName);
            }
        } else {
            LOG.warnf("Could not extract domain from email address '%s'", username);
        }

        return homeIdps;
    }

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
            linkedIdps = context.getSession().users()
                .getFederatedIdentitiesStream(context.getRealm(), user)
                .collect(
                    Collectors.toMap(FederatedIdentityModel::getIdentityProvider, FederatedIdentityModel::getUserName));
        }


        //Todo: This logic should be moved in a custom identity provider class. see OrgsIdentityProviders
        List<IdentityProviderModel> candidateIdps = identityProviders.candidatesForHomeIdp(context, user);
        if (candidateIdps == null) {
            candidateIdps = emptyList();
        }
        // Original; lookup mechanism from https://github.com/sventorben/keycloak-home-idp-discovery
        /*
        List<IdentityProviderModel> idpsWithMatchingDomain = identityProviders.withMatchingDomain(context, candidateIdps, domain);
        if (idpsWithMatchingDomain == null) {
            idpsWithMatchingDomain = emptyList();
        }
        */
        // Overidden lookup mechanism to lookup via organization domain
        OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
        List<IdentityProviderModel> idpsWithMatchingDomain =
            orgs.getOrganizationsStreamForDomain(
                    context.getRealm(), domain.toString(), config.requireVerifiedDomain())
                .flatMap(OrganizationModel::getIdentityProvidersStream)
                .filter(IdentityProviderModel::isEnabled)
                .collect(Collectors.toList());

        // Prefer linked IdP with matching domain first
        List<IdentityProviderModel> homeIdps = getLinkedIdpsFrom(idpsWithMatchingDomain, linkedIdps);

        if (homeIdps.isEmpty()) {
            if (!linkedIdps.isEmpty()) {
                // Prefer linked and enabled IdPs without matching domain in favor of not linked IdPs with matching domain
                homeIdps = getLinkedIdpsFrom(candidateIdps, linkedIdps);
            }
            if (homeIdps.isEmpty()) {
                // Fallback to not linked IdPs with matching domain (general case if user logs in for the first time)
                homeIdps = idpsWithMatchingDomain;
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

    private List<IdentityProviderModel> getLinkedIdpsFrom(List<IdentityProviderModel> enabledIdpsWithMatchingDomain, Map<String, String> linkedIdps) {
        return enabledIdpsWithMatchingDomain.stream()
            .filter(it -> linkedIdps.containsKey(it.getAlias()))
            .collect(Collectors.toList());
    }

    @Override
    public void close() {
    }
}
