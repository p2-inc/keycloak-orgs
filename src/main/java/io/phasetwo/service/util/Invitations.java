package io.phasetwo.service.util;

import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.UserModel;

@JBossLog
public final class Invitations {

  public static void memberFromInvitation(InvitationModel invitation, UserModel user) {
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
}
