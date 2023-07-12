package io.phasetwo.service.auth;

import io.phasetwo.service.auth.action.PortalLinkActionTokenHandler;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.TokenVerifier;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.VerificationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

@JBossLog
public class PortalLinkAuthenticator implements Authenticator {

  private final KeycloakSession session;

  public PortalLinkAuthenticator(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    final AuthenticationSessionModel authSession = context.getAuthenticationSession();
    final RealmModel realm = context.getRealm();

    String tokenString =
        authSession.getAuthNote(PortalLinkActionTokenHandler.ORIGINAL_ACTION_TOKEN);
    log.infof(
        "Got token string from auth note (%s): %s",
        PortalLinkActionTokenHandler.ORIGINAL_ACTION_TOKEN, tokenString);
    if (tokenString == null) {
      context.attempted();
      return;
    }

    try {
      JsonWebToken token = TokenVerifier.create(tokenString, JsonWebToken.class).getToken();

      // lookup the user
      log.infof("found user %s in token", token.getSubject());
      UserModel user = session.users().getUserById(realm, token.getSubject());
      context.setUser(user);
      // context.attachUserSession(authResult.getSession());
      context.success();

    } catch (VerificationException ex) {
      log.error("Error handling action token", ex);
      context.failure(
          AuthenticationFlowError.INTERNAL_ERROR,
          context
              .form()
              .setError(Messages.INVALID_PARAMETER)
              .createErrorPage(Status.INTERNAL_SERVER_ERROR));
    }
    context.attempted();
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}

  @Override
  public void action(AuthenticationFlowContext context) {
    context.success();
  }

  @Override
  public void close() {
    // NOOP
  }
}
