package io.phasetwo.service.resource;

import io.phasetwo.service.model.OrganizationModel;
import jakarta.ws.rs.ForbiddenException;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.*;

@JBossLog
public class OrganizationAdminPermissionEvaluator implements AdminPermissionEvaluator {

  private final AdminPermissionEvaluator permissions;
  private final OrganizationAdminAuth auth;
  private final OrganizationModel organization;

  public OrganizationAdminPermissionEvaluator(
      OrganizationModel organization,
      OrganizationAdminAuth auth,
      AdminPermissionEvaluator permissions) {
    this.organization = organization;
    this.auth = auth;
    this.permissions = permissions;
  }

  @Override
  public AdminAuth adminAuth() {
    return permissions.adminAuth();
  }

  @Override
  public void requireAnyAdminRole() {
    permissions.requireAnyAdminRole();
  }

  @Override
  public boolean hasOneAdminRole(String... adminRoles) {
    return permissions.hasOneAdminRole(adminRoles);
  }

  @Override
  public ClientPermissionEvaluator clients() {
    return permissions.clients();
  }

  @Override
  public GroupPermissionEvaluator groups() {
    return permissions.groups();
  }

  @Override
  public RealmPermissionEvaluator realm() {
    final RealmPermissionEvaluator realm = permissions.realm();
    return new RealmPermissionEvaluator() {
      @Override
      public boolean canListRealms() {
        return realm.canListRealms();
      }

      @Override
      public boolean canManageAuthorization(ResourceServer resourceServer) {
        return realm.canManageAuthorization(resourceServer);
      }

      @Override
      public boolean canManageEvents() {
        return realm.canManageEvents();
      }

      @Override
      public boolean canManageIdentityProviders() {
        log.debugf(
            "canManageIdentityProviders %b",
            (realm.canManageIdentityProviders()
                || auth.hasOrgManageIdentityProviders(organization)));
        // custom
        return (realm.canManageIdentityProviders()
            || auth.hasOrgManageIdentityProviders(organization));
      }

      @Override
      public boolean canManageRealm() {
        return realm.canManageRealm();
      }

      @Override
      public boolean canViewAuthorization(ResourceServer resourceServer) {
        return realm.canViewAuthorization(resourceServer);
      }

      @Override
      public boolean canViewEvents() {
        return realm.canViewEvents();
      }

      @Override
      public boolean canViewIdentityProviders() {
        log.debugf(
            "canViewIdentityProviders %b",
            (realm.canViewIdentityProviders() || auth.hasOrgViewIdentityProviders(organization)));
        // custom
        return (realm.canViewIdentityProviders() || auth.hasOrgViewIdentityProviders(organization));
      }

      @Override
      public boolean canViewRealm() {
        return realm.canViewRealm();
      }

      @Override
      public void requireManageAuthorization(ResourceServer resourceServer) {
        realm.requireManageAuthorization(resourceServer);
      }

      @Override
      public void requireManageEvents() {
        realm.requireManageEvents();
      }

      @Override
      public void requireManageIdentityProviders() {
        if (!canManageIdentityProviders()) {
          throw new ForbiddenException();
        }
      }

      @Override
      public void requireManageRealm() {
        realm.requireManageRealm();
      }

      @Override
      public void requireViewAuthenticationFlows() {
        realm.requireViewAuthenticationFlows();
      }

      @Override
      public void requireViewAuthorization(ResourceServer resourceServer) {
        realm.requireViewAuthorization(resourceServer);
      }

      @Override
      public void requireViewClientAuthenticatorProviders() {
        realm.requireViewClientAuthenticatorProviders();
      }

      @Override
      public void requireViewEvents() {
        realm.requireViewEvents();
      }

      @Override
      public void requireViewIdentityProviders() {
        if (!canViewIdentityProviders()) {
          throw new ForbiddenException();
        }
      }

      @Override
      public void requireViewRealm() {
        realm.requireViewRealm();
      }

      @Override
      public void requireViewRealmNameList() {
        realm.requireViewRealmNameList();
      }

      @Override
      public void requireViewRequiredActions() {
        realm.requireViewRequiredActions();
      }
    };
  }

  @Override
  public RolePermissionEvaluator roles() {
    return permissions.roles();
  }

  @Override
  public UserPermissionEvaluator users() {
    return permissions.users();
  }
}
