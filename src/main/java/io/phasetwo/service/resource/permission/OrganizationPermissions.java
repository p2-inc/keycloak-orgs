package io.phasetwo.service.resource.permission;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminAuth;

@JBossLog
public class OrganizationPermissions {

  protected final KeycloakSession session;
  protected final RealmModel realm;
  protected final AdminAuth auth;

  public OrganizationPermissions(KeycloakSession session, RealmModel realm, AdminAuth auth) {
    this.session = session;
    this.realm = realm;
    this.auth = auth;
  }

  public boolean canViewOrganization(String orgId) {
    return true;
  }

  public boolean canManageOrganization(String orgId) {
    return true;
  }

  // canViewTeam
  // canManageTeam
  // canViewInvitation
  // canManageInvitation
}
