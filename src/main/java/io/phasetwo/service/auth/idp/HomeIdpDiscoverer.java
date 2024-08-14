package io.phasetwo.service.auth.idp;

import io.phasetwo.service.model.OrganizationProvider;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

final class HomeIdpDiscoverer {

    private static final Logger LOG = Logger.getLogger(HomeIdpDiscoverer.class);

    private final DomainExtractor domainExtractor;
    private final AuthenticationFlowContext context;

    HomeIdpDiscoverer(AuthenticationFlowContext context) {
        this(new DomainExtractor(new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig())), context);
    }

    private HomeIdpDiscoverer(DomainExtractor domainExtractor, AuthenticationFlowContext context) {
        this.domainExtractor = domainExtractor;
        this.context = context;
    }

    public List<IdentityProviderModel> discoverForUser(String username) {

        String realmName = context.getRealm().getName();
        AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
        LOG.tracef("Trying to discover home IdP for username '%s' in realm '%s' with authenticator config '%s'",
            username, realmName, authenticatorConfig == null ? "<unconfigured>" : authenticatorConfig.getAlias());

        List<IdentityProviderModel> homeIdps = new ArrayList<>();

        final Optional<Domain> emailDomain;
        UserModel user = context.getUser();
        if (user == null) {
            LOG.tracef("No user found in AuthenticationFlowContext. Extracting domain from provided username '%s'.",
                username);
            emailDomain = domainExtractor.extractFrom(username);
        } else {
            LOG.tracef("User found in AuthenticationFlowContext. Extracting domain from stored user '%s'.",
                user.getId());
            emailDomain = domainExtractor.extractFrom(user);
        }

        HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(authenticatorConfig);
        if (config.requireVerifiedEmail()
            && "email".equalsIgnoreCase(config.userAttribute())
            && !user.isEmailVerified()) {
            LOG.debugf("Email of user %s not verified. Skipping discovery of linked IdPs", user.getId());
            return homeIdps;
        }

        if (emailDomain.isPresent()) {
            Domain domain = emailDomain.get();
            homeIdps = discoverHomeIdps(domain, user, username);
            if (homeIdps.isEmpty()) {
                LOG.debugf("Could not find home IdP for domain '%s' and user '%s' in realm '%s'",
                    domain, username, realmName);
            }
        } else {
            LOG.warnf("Could not extract domain from email address '%s'", username);
        }

        return homeIdps;
    }

    // Note(fastly):
    //
    // Fastly implementation of discoverHomeIdps
    // See above function for original implementation.
    //
    private List<IdentityProviderModel> discoverHomeIdps(Domain domain, UserModel user, String username) {
        final Map<String, String> linkedIdps;

        HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());
        if (user == null || !config.forwardToLinkedIdp()) {
            LOG.tracef(
                "User '%s' is not stored locally or forwarding to linked IdP is disabled. Skipping discovery of linked IdPs.",
                username);
            return Collections.emptyList();
        }

        LOG.tracef(
            "Found local user '%s' and forwarding to linked IdP is enabled. Discovering linked IdPs.",
            username);

        linkedIdps = context.getSession().users()
                .getFederatedIdentitiesStream(context.getRealm(), user)
                .collect(
                    Collectors.toMap(FederatedIdentityModel::getIdentityProvider, FederatedIdentityModel::getUserName));

        // Custom Fastly lookup mechanism.
        //
        // 1. Get all Orgs linked to the user
        // 3. Filter orgs based on inbound client (i.e. only return Sig-Sci orgs for sig-sci client etc)
        // 4. Filter orgs to only those with force_sso
        // 2. Filter to only enabled IdPs
        String clientID = context.getAuthenticationSession().getClient().getClientId();
        OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
        String userDefaultCID = Objects.toString(
                user.getFirstAttribute("default_cid"),
            "");

        List<IdentityProviderModel> enabledIdpsForUserOrgs =
            orgs.getUserOrganizationsStream(
                    context.getRealm(), user)
                .filter(o -> {
                    if(clientID.equals("sigsci-dashboard")) {
                        String corp = o.getFirstAttribute("corp_id");
                        boolean hasCorpID = corp != null && !corp.isEmpty();
                        return hasCorpID;
                    } else if (clientID.contains("zendesk")) {
                        String corp = o.getFirstAttribute("corp_id");
                        if (corp != null && !corp.isEmpty()) {
                            return true;
                        }
                    }
                    String cid = o.getFirstAttribute("customer_id");
                    boolean hasCID = cid != null && !cid.isEmpty();
                    String forceSSO = o.getFirstAttribute("force_sso");
                    boolean hasForceSSO = forceSSO != null && forceSSO.equals("1");
                    return hasCID && hasForceSSO;
                })
                .sorted((o1, o2) -> {
                    if(o1.getFirstAttribute("customer_id") == userDefaultCID) return -1;
                    else return 1;
                })
                .flatMap(o -> o.getIdentityProvidersStream())
                .filter(IdentityProviderModel::isEnabled)
                .collect(Collectors.toList());

        List<IdentityProviderModel> homeIdps = getLinkedIdpsFrom(enabledIdpsForUserOrgs, linkedIdps);

        logFoundIdps("linked", "matching", homeIdps, domain, username);

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
}
