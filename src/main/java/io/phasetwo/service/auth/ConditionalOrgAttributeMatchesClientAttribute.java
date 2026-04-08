package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import io.phasetwo.service.model.OrganizationProvider;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/**
 * Conditional authenticator that checks whether the authenticating user belongs to a Phase Two
 * organization whose attribute matches a value read from the current OIDC client's attributes.
 *
 * <p>This enables per-client tenant enforcement with a single realm-wide Post Broker Login flow.
 *
 * <p><b>IMPORTANT:</b> This authenticator requires a user and must be placed in Post Broker Login
 * or First Broker Login flows, NOT in the Browser flow before IdP redirect completes.
 */
@JBossLog
public class ConditionalOrgAttributeMatchesClientAttribute implements ConditionalAuthenticator {

  static final ConditionalOrgAttributeMatchesClientAttribute SINGLETON =
      new ConditionalOrgAttributeMatchesClientAttribute();

  @Override
  public boolean matchCondition(AuthenticationFlowContext context) {
    // Retrieve configuration
    Map<String, String> config = context.getAuthenticatorConfig().getConfig();
    String orgAttrName =
        config.get(
            ConditionalOrgAttributeMatchesClientAttributeFactory.CONF_ORG_ATTRIBUTE_NAME);
    String clientAttrName =
        config.get(
            ConditionalOrgAttributeMatchesClientAttributeFactory.CONF_CLIENT_ATTRIBUTE_NAME);
    boolean negateOutput =
        Boolean.parseBoolean(
            config.get(ConditionalOrgAttributeMatchesClientAttributeFactory.CONF_NEGATE));

    // Validate config — fail closed if misconfigured
    if (orgAttrName == null || orgAttrName.isBlank() || clientAttrName == null
        || clientAttrName.isBlank()) {
      log.warnf(
          "Condition config incomplete: orgAttr=[%s], clientAttr=[%s]. Returning no-match (fail closed).",
          orgAttrName, clientAttrName);
      return negateOutput;
    }

    // Read expected value from the current client's attributes
    ClientModel client = context.getAuthenticationSession().getClient();
    String expectedValue = client.getAttribute(clientAttrName);

    if (expectedValue == null || expectedValue.isBlank()) {
      log.infof(
          "Client [%s] has no attribute [%s]. Returning no-match (fail closed).",
          client.getClientId(), clientAttrName);
      return negateOutput;
    }

    // Get Phase Two OrganizationProvider
    KeycloakSession session = context.getSession();
    RealmModel realm = context.getRealm();
    UserModel user = context.getUser();

    OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
    if (orgProvider == null) {
      log.errorf(
          "OrganizationProvider not available. Is Phase Two keycloak-orgs deployed? Returning no-match (fail closed).");
      return negateOutput;
    }

    // Check user's org memberships for a matching attribute
    boolean match =
        orgProvider
            .getUserOrganizationsStream(realm, user)
            .anyMatch(
                org -> {
                  boolean orgMatch;
                  try {
                    orgMatch = org.hasAttribute(orgAttrName, expectedValue);
                  } catch (NoSuchMethodError e) {
                    // Fallback for older Phase Two versions
                    orgMatch =
                        org.getAttributes()
                            .getOrDefault(orgAttrName, java.util.List.of())
                            .contains(expectedValue);
                  }
                  if (orgMatch) {
                    log.debugf(
                        "Org [%s] (id=%s) matches: %s=%s",
                        org.getName(), org.getId(), orgAttrName, expectedValue);
                  }
                  return orgMatch;
                });

    log.infof(
        "Condition for user [%s], client [%s]: orgAttr[%s] == clientAttr[%s](value=[%s]) → match=%b, negate=%b, final=%b",
        user.getUsername(),
        client.getClientId(),
        orgAttrName,
        clientAttrName,
        expectedValue,
        match,
        negateOutput,
        negateOutput != match);

    return negateOutput != match;
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    // no-op
  }

  @Override
  public boolean requiresUser() {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }
}
