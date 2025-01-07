package io.phasetwo.service.broker;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.RealmModel;

@JBossLog
public class Mappers {

  public static OrganizationRoleModel getOrganizationRole(
      OrganizationProvider orgs, String orgName, String orgRoleName, RealmModel realm) {
    OrganizationModel org = orgs.getOrganizationByName(realm, orgName);
    if (org == null) {
      log.debugf("Cannot map non-existent org %s", orgName);
      return null;
    }
    OrganizationRoleModel role = org.getRoleByName(orgRoleName);
    if (role == null) {
      log.debugf("Cannot map non-existent org role %s - %s", orgName, orgRoleName);
      return null;
    }
    return role;
  }
}
