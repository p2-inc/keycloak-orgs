package org.keycloak.services.resources.admin.permissions;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authorization.identity.UserModelIdentity;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.RealmManager;

@JBossLog
public class Permissions {

  public static AdminPermissionEvaluator evaluator(
      KeycloakSession session, RealmModel realm, RealmModel adminRealm, UserModel admin) {
    // hold onto this logging for a while
    if (log.isTraceEnabled()) {
      log.tracef("realm %s adminRealm %s", realm.getName(), adminRealm.getName());
      String clientId = null;
      RealmManager realmManager = new RealmManager(session);
      if (RealmManager.isAdministrationRealm(adminRealm)) {
        log.tracef("isAdministrationRealm %s", adminRealm.getName());
        clientId = realm.getMasterAdminClient().getClientId();
      } else if (adminRealm.equals(realm)) {
        log.tracef("adminRealm.equals(realm)) %s", realm.getName());
        clientId =
            realm.getClientByClientId(realmManager.getRealmAdminClientId(realm)).getClientId();
      }
      log.tracef("permissions clientId %s", clientId);
      ClientModel client = realm.getClientByClientId(clientId);
      log.tracef("permissions clientId model is %s", client);
    }
    if (RealmManager.isAdministrationRealm(adminRealm) || !adminRealm.equals(realm)) {
      MgmtPermissions perm = new MgmtPermissions(session, realm, adminRealm, admin);
      perm.identity = new UserModelIdentity(adminRealm, admin);
      return perm;
    } else {
      return AdminPermissions.evaluator(session, realm, adminRealm, admin);
    }
  }
}
