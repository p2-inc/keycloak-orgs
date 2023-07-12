package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.*;

import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.PostBrokerLoginConstants;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

@JBossLog
public class PostOrgAuthFlow {

  static void setStatus(AuthenticationFlowContext context) {
    if (context.getExecution().getRequirement()
        == AuthenticationExecutionModel.Requirement.REQUIRED) {
      context.success();
    } else {
      context.attempted();
    }
  }

  static BrokeredIdentityContext getBrokeredIdentityContext(AuthenticationFlowContext context) {
    AuthenticationSessionModel clientSession = context.getAuthenticationSession();
    SerializedBrokeredIdentityContext serializedCtx =
        SerializedBrokeredIdentityContext.readFromAuthenticationSession(
            clientSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
    if (serializedCtx == null) {
      // get the identity context for post login flow
      // https://groups.google.com/g/keycloak-user/c/eiX5dCjN3qc
      serializedCtx =
          SerializedBrokeredIdentityContext.readFromAuthenticationSession(
              clientSession, PostBrokerLoginConstants.PBL_BROKERED_IDENTITY_CONTEXT);
    }
    if (serializedCtx == null) {
      throw new AuthenticationFlowException(
          "Not found serialized context in clientSession",
          AuthenticationFlowError.IDENTITY_PROVIDER_ERROR);
    }
    return serializedCtx.deserialize(context.getSession(), clientSession);
  }

  static boolean brokeredIdpEnabled(
      AuthenticationFlowContext context, BrokeredIdentityContext brokerContext) {
    if (!brokerContext.getIdpConfig().isEnabled()) {
      context.getEvent().user(context.getUser()).error(Errors.IDENTITY_PROVIDER_ERROR);
      Response challengeResponse =
          context
              .form()
              .setError(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR)
              .createErrorPage(Response.Status.BAD_REQUEST);
      context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challengeResponse);
      return false;
    } else {
      return true;
    }
  }

  static void realmPostCreate(RealmModel.RealmPostCreateEvent event, String providerId) {
    KeycloakSession session = event.getKeycloakSession();
    RealmModel realm = event.getCreatedRealm();
    AuthenticationFlowModel flow = realm.getFlowByAlias(ORG_AUTH_FLOW_ALIAS);
    if (flow == null) {
      log.infof("creating built-in auth flow for %s", ORG_AUTH_FLOW_ALIAS);
      flow = new AuthenticationFlowModel();
      flow.setAlias(ORG_AUTH_FLOW_ALIAS);
      flow.setBuiltIn(true);
      flow.setProviderId("basic-flow");
      flow.setDescription("Post broker login flow used for organization IdPs.");
      flow.setTopLevel(true);
      flow = realm.addAuthenticationFlow(flow);
    }

    boolean hasExecution =
        realm
                .getAuthenticationExecutionsStream(flow.getId())
                .filter(e -> providerId.equals(e.getAuthenticator()))
                .count()
            > 0;

    if (!hasExecution) {
      log.infof("adding execution %s for auth flow for %s", providerId, ORG_AUTH_FLOW_ALIAS);
      ProviderFactory f =
          session.getKeycloakSessionFactory().getProviderFactory(Authenticator.class, providerId);
      AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
      execution.setParentFlow(flow.getId());
      execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
      execution.setAuthenticatorFlow(false);
      execution.setAuthenticator(providerId);
      execution = realm.addAuthenticatorExecution(execution);
    }
  }
}
