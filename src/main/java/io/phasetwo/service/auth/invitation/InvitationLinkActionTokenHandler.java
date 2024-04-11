package io.phasetwo.service.auth.invitation;

import static io.phasetwo.service.Orgs.*;

import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.events.*;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;

/**
 * Handles the invitation link action token by logging the user in and forwarding to the redirect
 * uri.
 */
@JBossLog
public class InvitationLinkActionTokenHandler
    extends AbstractActionTokenHandler<InvitationLinkActionToken> {

  public InvitationLinkActionTokenHandler() {
    super(
        InvitationLinkActionToken.TOKEN_TYPE,
        InvitationLinkActionToken.class,
        Messages.INVALID_REQUEST,
        EventType.EXECUTE_ACTION_TOKEN,
        Errors.INVALID_REQUEST);
  }

  @Override
  public Response handleToken(
      InvitationLinkActionToken token, ActionTokenContext<InvitationLinkActionToken> tokenContext) {
    EventBuilder event = tokenContext.getEvent();
    log.infof(
        "handleToken for iss:%s, org:%s, user:%s, rdu:%s",
        token.getIssuedFor(), token.getOrgId(), token.getUserId(), token.getRedirectUri());

    // Handle user registration if necessary. Email should be in the subject/userId
    String email = token.getUserId();
    UserModel user =
        KeycloakModelUtils.findUserByNameOrEmail(
            tokenContext.getSession(), tokenContext.getRealm(), email);
    if (user == null) {
      log.infof("force creating user for %s", email);
      // create the user with the given email and set enabled / email verified
      user = tokenContext.getSession().users().addUser(tokenContext.getRealm(), email);
      user.setEnabled(true);
      user.setEmail(email);
      user.setEmailVerified(true);
      // emit a registration event
      event
          .event(EventType.REGISTER)
          .detail(Details.REGISTER_METHOD, "invitation")
          .detail(Details.USERNAME, user.getUsername())
          .detail(Details.EMAIL, user.getEmail())
          .user(user)
          .success();
    }

    // now set the user to the authentication session
    tokenContext.getAuthenticationSession().setAuthenticatedUser(user);

    // explicitly add the invitation required action, if they still have active invitations
    long cnt =
        InvitationRequiredAction.getUserInvites(
                tokenContext.getSession(), tokenContext.getRealm(), user)
            .count();
    if (cnt > 0) {
      log.infof("Adding InvitationRequiredActionFactory for %s", user.getEmail());
      user.addRequiredAction(InvitationRequiredActionFactory.PROVIDER_ID);
    }

    String nextAction =
        (cnt > 0)
            ? InvitationRequiredActionFactory.PROVIDER_ID
            : AuthenticationManager.nextRequiredAction(
                tokenContext.getSession(),
                tokenContext.getAuthenticationSession(),
                tokenContext.getRequest(),
                tokenContext.getEvent());
    return AuthenticationManager.redirectToRequiredActions(
        tokenContext.getSession(),
        tokenContext.getRealm(),
        tokenContext.getAuthenticationSession(),
        tokenContext.getUriInfo(),
        nextAction);

    /*

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

    event.detail(FIELD_ORG_ID, token.getOrgId()).success();

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
    */
  }
}
