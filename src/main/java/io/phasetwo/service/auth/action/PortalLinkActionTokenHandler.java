package io.phasetwo.service.auth.action;

import static io.phasetwo.service.Orgs.*;

import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.events.*;
import org.keycloak.models.ClientModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.util.ResolveRelative;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Handles the portal link action token by logging the user in and forwarding to the redirect uri.
 */
@JBossLog
public class PortalLinkActionTokenHandler
    extends AbstractActionTokenHandler<PortalLinkActionToken> {

  public PortalLinkActionTokenHandler() {
    super(
        PortalLinkActionToken.TOKEN_TYPE,
        PortalLinkActionToken.class,
        Messages.INVALID_REQUEST,
        EventType.EXECUTE_ACTION_TOKEN,
        Errors.INVALID_REQUEST);
  }

  /*
  @Override
  public Predicate<? super PortalLinkActionToken>[] getVerifiers(
      ActionTokenContext<PortalLinkActionToken> tokenContext) {
    return TokenUtils.predicates(
        TokenUtils.checkThat(
            t ->
                Objects.equals(
                    t.getEmail(),
                    tokenContext.getAuthenticationSession().getAuthenticatedUser().getEmail()),
            Errors.INVALID_EMAIL,
            getDefaultErrorMessage()));
  }
  */
  public static final String ORIGINAL_ACTION_TOKEN = "ORIGINAL_ACTION_TOKEN";

  @Override
  public AuthenticationSessionModel startFreshAuthenticationSession(
      PortalLinkActionToken token, ActionTokenContext<PortalLinkActionToken> tokenContext) {
    return tokenContext.createAuthenticationSessionForClient(token.getIssuedFor());
  }

  @Override
  public Response handleToken(
      PortalLinkActionToken token, ActionTokenContext<PortalLinkActionToken> tokenContext) {
    log.infof(
        "handleToken for iss:%s, org:%s, user:%s, rdu:%s",
        token.getIssuedFor(), token.getOrgId(), token.getUserId(), token.getRedirectUri());
    UserModel user = tokenContext.getAuthenticationSession().getAuthenticatedUser();

    final AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
    final ClientModel client = authSession.getClient();
    final String redirectUri =
        token.getRedirectUri() != null
            ? token.getRedirectUri()
            : ResolveRelative.resolveRelativeUri(
                tokenContext.getSession(), client.getRootUrl(), client.getBaseUrl());
    log.infof("Using client_id %s redirect_uri %s", client.getClientId(), redirectUri);

    String redirect =
        RedirectUtils.verifyRedirectUri(
            tokenContext.getSession(), redirectUri, authSession.getClient());
    log.infof("Redirect after verify %s -> %s", redirectUri, redirect);
    if (redirect != null) {
      authSession.setAuthNote(
          AuthenticationManager.SET_REDIRECT_URI_AFTER_REQUIRED_ACTIONS, "true");
      authSession.setRedirectUri(redirect);
      authSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, redirectUri);
    }

    // set the orgId to a user session note
    authSession.setUserSessionNote(FIELD_ORG_ID, token.getOrgId());

    String nextAction =
        AuthenticationManager.nextRequiredAction(
            tokenContext.getSession(),
            authSession,
            tokenContext.getRequest(),
            tokenContext.getEvent());
    return AuthenticationManager.redirectToRequiredActions(
        tokenContext.getSession(),
        tokenContext.getRealm(),
        authSession,
        tokenContext.getUriInfo(),
        nextAction);

    // This doesn't work. Why?
    // return tokenContext.processFlow(true, AUTHENTICATE_PATH,
    // tokenContext.getRealm().getBrowserFlow(), null, new AuthenticationProcessor());
  }
}
