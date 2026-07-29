package io.phasetwo.service.auth.invitation;

import static org.keycloak.events.EventType.CUSTOM_REQUIRED_ACTION;

import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.util.Invitations;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@JBossLog
public class InvitationAutoJoinAuthenticator implements Authenticator {

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.debug("InvitationAutoJoinAuthenticator.authenticate");
    membershipFromInvitations(context);
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.debug("InvitationAutoJoinAuthenticator.action");
  }

  private void membershipFromInvitations(AuthenticationFlowContext context) {
    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    var user = context.getUser();
    var invitations = orgs.getUserInvitationsStream(context.getRealm(), user);
    EventBuilder event = context.getEvent();
    invitations.forEach(
        i -> {
          // add membership
          log.debugf("invitation processed %s", i.getId());

          Invitations.memberFromInvitation(i, user);
          event
              .clone()
              .event(CUSTOM_REQUIRED_ACTION)
              .user(user)
              .detail("org_id", i.getOrganization().getId())
              .detail("invitation_id", i.getId())
              .success();

          // revoke invitation
          i.getOrganization().revokeInvitation(i.getId());
          event
              .clone()
              .event(CUSTOM_REQUIRED_ACTION)
              .detail("org_id", i.getOrganization().getId())
              .detail("invitation_id", i.getId())
              .user(user)
              .error("User invitation revoked.");
        });

    context.success();
  }

  @Override
  public boolean requiresUser() {
    return true;
  }

  @Override
  public boolean configuredFor(
      KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    return true;
  }

  @Override
  public void setRequiredActions(
      KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {}

  @Override
  public void close() {}
}
