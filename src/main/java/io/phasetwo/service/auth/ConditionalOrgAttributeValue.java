package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@JBossLog
public class ConditionalOrgAttributeValue implements ConditionalAuthenticator {

  static final ConditionalOrgAttributeValue SINGLETON = new ConditionalOrgAttributeValue();

  @Override
  public boolean matchCondition(AuthenticationFlowContext context) {
    // Retrieve configuration
    Map<String, String> config = context.getAuthenticatorConfig().getConfig();
    String attributeName = config.get(ConditionalOrgAttributeValueFactory.CONF_ATTRIBUTE_NAME);
    String attributeValue =
        config.get(ConditionalOrgAttributeValueFactory.CONF_ATTRIBUTE_EXPECTED_VALUE);
    boolean allOrgs =
        Boolean.parseBoolean(config.get(ConditionalOrgAttributeValueFactory.CONF_ALL_ORGS));
    boolean negateOutput =
        Boolean.parseBoolean(config.get(ConditionalOrgAttributeValueFactory.CONF_NOT));

    // Check org(s) for attribute
    boolean match = false;
    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    if (!allOrgs) {
      match =
          context
              .getUser()
              .getAttributeStream(ACTIVE_ORGANIZATION)
              .findFirst()
              .map(
                  orgId -> {
                    OrganizationModel o = orgs.getOrganizationById(context.getRealm(), orgId);
                    return o.hasMembership(context.getUser())
                        && o.hasAttribute(attributeName, attributeValue);
                  })
              .orElse(false);
    } else {
      match =
          orgs.getUserOrganizationsStream(context.getRealm(), context.getUser())
              .anyMatch(o -> o.hasAttribute(attributeName, attributeValue));
    }

    log.infof("Testing org attribute %s == %s ? %b", attributeName, attributeValue, match);

    return negateOutput != match;
  }

  @Override
  public void action(AuthenticationFlowContext context) {}

  @Override
  public boolean requiresUser() {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

  @Override
  public void close() {}
}
