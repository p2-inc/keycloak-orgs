package io.phasetwo.service.auth.idp.discovery.extattribute;

import io.phasetwo.service.auth.idp.PublicAPI;
import io.phasetwo.service.auth.idp.Users;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.*;
import org.keycloak.utils.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@PublicAPI(unstable = true)
public final class UserAttributeHomeIdpDiscoverer implements HomeIdpDiscoverer {

    private static final Logger LOG = Logger.getLogger(UserAttributeHomeIdpDiscoverer.class);

    private final Users users;
    private final IdentityProviders identityProviders;

    @PublicAPI(unstable = true)
    public UserAttributeHomeIdpDiscoverer(Users users, IdentityProviders identityProviders) {
        this.users = users;
        this.identityProviders = identityProviders;
    }

    @Override
    public List<IdentityProviderModel> discoverForUser(AuthenticationFlowContext context, String username) {
        UserAttributeHomeIdpDiscovererConfig config = new UserAttributeHomeIdpDiscovererConfig(context.getAuthenticatorConfig());

        String realmName = context.getRealm().getName();
        LOG.tracef("Trying to discover home IdP for username '%s' in realm '%s' with authenticator config '%s'",
                username, realmName, config.getAlias());

        List<IdentityProviderModel> homeIdps = new ArrayList<>();

        UserModel user = users.lookupBy(username);
        String attribute;
        if (user == null) {
            LOG.tracef("No user found in AuthenticationFlowContext. Extracting domain from provided username '%s'.",
                    username);
            return List.of();
        } else {
            LOG.tracef("User found in AuthenticationFlowContext. Extracting domain from stored user '%s'.",
                    user.getId());
            var userAttributeName = config.userAttribute();
            attribute = user.getFirstAttribute(userAttributeName);
        }

        if (!StringUtil.isNullOrEmpty(attribute)) {
            homeIdps = discoverHomeIdps(context, attribute, user, username);
            if (homeIdps.isEmpty()) {
                LOG.infof("Could not find home IdP for attribute '%s' and user '%s' in realm '%s'",
                        attribute, username, realmName);
            }
        } else {
            LOG.warnf("Could not extract attribute '%s'", username);
        }

        return homeIdps;
    }

    private List<IdentityProviderModel> discoverHomeIdps(AuthenticationFlowContext context, String attribute, UserModel user, String username) {
        final Map<String, String> linkedIdps;

        UserAttributeHomeIdpDiscovererConfig config = new UserAttributeHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
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

        List<IdentityProviderModel> candidateIdps = identityProviders.candidatesForHomeIdp(context, user);
        if (candidateIdps == null) {
            candidateIdps = emptyList();
        }
        List<IdentityProviderModel> idpsWithMatchingDomain = identityProviders.withMatchingAttribute(context, candidateIdps, attribute);
        if (idpsWithMatchingDomain == null) {
            idpsWithMatchingDomain = emptyList();
        }

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
                logFoundIdps("non-linked", "matching", homeIdps, attribute, username);
            } else {
                logFoundIdps("non-linked", "non-matching", homeIdps, attribute, username);
            }
        } else {
            logFoundIdps("linked", "matching", homeIdps, attribute, username);
        }

        return homeIdps;
    }

    private void logFoundIdps(String idpQualifier, String domainQualifier, List<IdentityProviderModel> homeIdps, String attribute, String username) {
        String homeIdpsString = homeIdps.stream()
                .map(IdentityProviderModel::getAlias)
                .collect(Collectors.joining(","));
        LOG.tracef("Found %s IdPs [%s] with %s attribute '%s' for user '%s'",
                idpQualifier, homeIdpsString, domainQualifier, attribute, username);
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
