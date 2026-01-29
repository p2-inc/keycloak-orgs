package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.IDP_VALIDATE_FLOW_ALIAS;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderFactory;

@JBossLog
public class IdpValidateAuthFlow {

  static void realmPostCreate(RealmModel.RealmPostCreateEvent event) {
    KeycloakSession session = event.getKeycloakSession();
    RealmModel realm = event.getCreatedRealm();
    AuthenticationFlowModel flow = realm.getFlowByAlias(IDP_VALIDATE_FLOW_ALIAS);
    if (flow == null) {
      log.infof("creating built-in auth flow for %s", IDP_VALIDATE_FLOW_ALIAS);
      flow = new AuthenticationFlowModel();
      flow.setAlias(IDP_VALIDATE_FLOW_ALIAS);
      flow.setBuiltIn(true);
      flow.setProviderId("basic-flow");
      flow.setDescription(
          "Authentication flow used to validate newly created organization identity providers.");
      flow.setTopLevel(true);
      flow = realm.addAuthenticationFlow(flow);
    }

    boolean hasExecution =
        realm
                .getAuthenticationExecutionsStream(flow.getId())
                .filter(e -> "identity-provider-redirector".equals(e.getAuthenticator()))
                .count()
            > 0;

    if (!hasExecution) {
      log.infof(
          "adding execution %s for auth flow for %s",
          "identity-provider-redirector",
          IDP_VALIDATE_FLOW_ALIAS);
      ProviderFactory f =
          session
              .getKeycloakSessionFactory()
              .getProviderFactory(Authenticator.class, "identity-provider-redirector");
      AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
      execution.setParentFlow(flow.getId());
      execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
      execution.setAuthenticatorFlow(false);
      execution.setAuthenticator("identity-provider-redirector");
      execution = realm.addAuthenticatorExecution(execution);
    }
  }
}
