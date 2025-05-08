package io.phasetwo.service.auth.invitation;

import static org.keycloak.events.EventType.CUSTOM_REQUIRED_ACTION;

import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/** */
@JBossLog
public class InvitationRequiredAction implements RequiredActionProvider {

  public InvitationRequiredAction() {}

  @Override
  public InitiatedActionSupport initiatedActionSupport() {
    return InitiatedActionSupport.SUPPORTED;
  }

  @Override
  public void evaluateTriggers(RequiredActionContext context) {
    RealmModel realm = context.getRealm();
    UserModel user = context.getUser();
    log.debugf(
        "InvitationRequiredAction.evaluateTriggers called for realm %s and user %s",
        realm.getName(), user.getEmail());

    long cnt = getUserInvites(context, realm, user).count();
    log.debugf("Found %d invites for %s", cnt, user.getEmail());
    if (cnt > 0) {
      log.debugf("Adding InvitationRequiredActionFactory for %s", user.getEmail());
      user.addRequiredAction(InvitationRequiredActionFactory.PROVIDER_ID);
    }
  }

  private Stream<InvitationModel> getUserInvites(
      RequiredActionContext context, RealmModel realm, UserModel user) {
    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    return orgs.getUserInvitationsStream(realm, user);
  }

  @Override
  public void requiredActionChallenge(RequiredActionContext context) {
    RealmModel realm = context.getRealm();
    UserModel user = context.getUser();
    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    log.debugf(
        "InvitationRequiredAction.requiredActionChallenge called for realm %s and user %s",
        realm.getName(), user.getEmail());
    if (user.isEmailVerified() && user.getEmail() != null) {
      List<InvitationModel> invites =
          getUserInvites(context, realm, user).collect(Collectors.toList());
      if (invites != null && invites.size() > 0) {
        log.debugf("Found %d invites for %s", invites.size(), user.getEmail());
        InvitationsBean ib = new InvitationsBean(realm, invites);
        Response challenge =
            context.form().setAttribute("invitations", ib).createForm("invitations.ftl");
        context.challenge(challenge);
        return;
      }
    }
    log.debug("No challenge");
    context.ignore();
  }

  @Override
  public void processAction(RequiredActionContext context) {
    EventBuilder event = context.getEvent();

    RealmModel realm = context.getRealm();
    UserModel user = context.getUser();
    log.debugf(
        "InvitationRequiredAction.processAction called for realm %s and user %s",
        realm.getName(), user.getEmail());

    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

    log.debugf("Form Parameters: %s", mapToString(formData));
    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    List<String> selected = formData.get("orgs");
    orgs.getUserInvitationsStream(realm, user)
        .forEach(
            i -> {
              if (selected != null && selected.contains(i.getOrganization().getId())) {
                // add membership
                log.debugf("selected %s", i.getOrganization().getId());
                memberFromInvitation(i, user);
                event
                    .clone()
                    .event(CUSTOM_REQUIRED_ACTION)
                    .user(user)
                    .detail("org_id", i.getOrganization().getId())
                    .detail("invitation_id", i.getId())
                    .success();
              }
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

  void memberFromInvitation(InvitationModel invitation, UserModel user) {
    // membership
    invitation.getOrganization().grantMembership(user);
    // roles
    invitation.getRoles().stream()
        .forEach(
            r -> {
              OrganizationRoleModel role = invitation.getOrganization().getRoleByName(r);
              if (role == null) {
                log.debugf("No org role found for invitation role %s. Skipping...", r);
              } else {
                role.grantRole(user);
              }
            });
  }

  @Override
  public void close() {}

  private String mapToString(Map<?, ?> map) {
    return map.keySet().stream()
        .map(key -> key + "=" + map.get(key))
        .collect(Collectors.joining(", ", "{", "}"));
  }
}
