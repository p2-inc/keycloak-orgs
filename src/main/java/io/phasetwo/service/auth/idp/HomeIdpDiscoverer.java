//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import static io.phasetwo.service.Orgs.*;

import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.util.IdentityProviders;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private List<IdentityProviderModel> discoverHomeIdps(Domain domain, UserModel user, String username) {
        final Map<String, String> linkedIdps;

        HomeIdpDiscoveryConfig config = new HomeIdpDiscoveryConfig(context.getAuthenticatorConfig());
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

        List<IdentityProviderModel> enabledIdps = determineEnabledIdps();
        // Original; lookup mechanism from https://github.com/sventorben/keycloak-home-idp-discovery
        /*
        List<IdentityProviderModel> enabledIdpsWithMatchingDomain = filterIdpsWithMatchingDomainFrom(enabledIdps,
            domain,
            config);
        */
        // Overidden lookup mechanism to lookup via organization domain
        OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
        List<IdentityProviderModel> enabledIdpsWithMatchingDomain =
            orgs.getOrganizationsStreamForDomain(
                    context.getRealm(), domain.toString(), config.requireVerifiedDomain())
                .flatMap(OrganizationModel::getIdentityProvidersStream)
                .filter(IdentityProviderModel::isEnabled)
                .collect(Collectors.toList());

        // If multi-idps is turned on, get a subset of that list with domain matches in the config.
        if (IdentityProviders.isMultipleIdpsConfigEnabled(context.getRealm())) {
          List<IdentityProviderModel> domainMatchingIdps =
              enabledIdpsWithMatchingDomain
              .stream()
              .filter(idp -> {
                  String domains = idp.getConfig().get(ORG_DOMAIN_CONFIG_KEY);
                  if (Strings.isNullOrEmpty(domains)) return false;
                  return IdentityProviders.strListContains(domains, domain.toString());
                })
              .distinct() // for shared IDP case
              .collect(Collectors.toList());
          // If there are _any_ matches, use that list. If there are none, use the original list.
          if (domainMatchingIdps.size() > 0) {
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

    private List<IdentityProviderModel> filterIdpsWithMatchingDomainFrom(List<IdentityProviderModel> enabledIdps, Domain domain, HomeIdpDiscoveryConfig config) {
        String userAttributeName = config.userAttribute();
        List<IdentityProviderModel> idpsWithMatchingDomain = enabledIdps.stream()
            .filter(it -> new IdentityProviderModelConfig(it).supportsDomain(userAttributeName, domain))
            .collect(Collectors.toList());
        LOG.tracef("IdPs with matching domain '%s' for attribute '%s': %s", domain, userAttributeName,
            idpsWithMatchingDomain.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return idpsWithMatchingDomain;
    }

  /**
   * @returns A list of all enabled idps for a realm.
   */
    private List<IdentityProviderModel> determineEnabledIdps() {
        RealmModel realm = context.getRealm();
        List<IdentityProviderModel> enabledIdps = context.getSession().identityProviders().getAllStream()
            .filter(IdentityProviderModel::isEnabled)
            .collect(Collectors.toList());
        LOG.tracef("Enabled IdPs in realm '%s': %s",
            realm.getName(),
            enabledIdps.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return enabledIdps;
    }

}
