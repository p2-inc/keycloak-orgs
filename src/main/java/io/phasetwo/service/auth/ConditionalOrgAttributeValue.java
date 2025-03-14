package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
    final AtomicBoolean match = new AtomicBoolean(false);
    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    if (!allOrgs) {
      context
          .getUser()
          .getAttributeStream(ACTIVE_ORGANIZATION)
          .findFirst()
          .ifPresent(
              orgId -> {
                if (orgId != null) {
                  OrganizationModel o = orgs.getOrganizationById(context.getRealm(), orgId);
                  if (o.hasMembership(context.getUser())
                      && o.hasAttribute(attributeName, attributeValue)) {
                    match.set(true);
                  }
                }
              });
    } else {
      orgs.getUserOrganizationsStream(context.getRealm(), context.getUser())
          .forEach(
              o -> {
                if (o.hasAttribute(attributeName, attributeValue)) {
                  match.set(true);
                }
              });
    }

    log.infof("Testing org attribute %s == %s ? %b", attributeName, attributeValue, match.get());

    return negateOutput != match.get();
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
