package io.phasetwo.service.auth.invitation;

import io.phasetwo.service.model.OrganizationProvider;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/** */
@JBossLog
public class InvitationAuthenticator implements Authenticator {

  public InvitationAuthenticator() {}

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.info("InvitationAuthenticator.authenticate called");
    context.success(); // There was no failure or challenge.
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.info("InvitationAuthenticator.action called");
    context.attempted(); // There was no failure or challenge.
  }

  @Override
  public boolean requiresUser() {
    return true; // we must know the user's email to look up the invitation
  }

  @Override
  public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    log.infof(
        "InvitationAuthenticator.configuredFor called for realm %s and user %s",
        realm.getName(), user.getEmail());

    // this is where we test to see if the authenticator is enabled
    // AND the user has an open org invitation return FALSE if we
    // find an invite. This is because the interface assumes an
    // "authenticated" notion, which is interpreted in this case as
    // "they're okay. no need to run the authenticator".
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    long cnt = orgs.getUserInvitationsStream(realm, user).count();
    log.infof("Found %d invites for %s", cnt, user.getEmail());
    return !(cnt > 0);
  }

  @Override
  public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    log.infof(
        "InvitationAuthenticator.setRequiredActions called for realm %s and user %s",
        realm.getName(), user.getEmail());
    user.addRequiredAction(InvitationRequiredActionFactory.PROVIDER_ID);
  }

  @Override
  public void close() {}
}
