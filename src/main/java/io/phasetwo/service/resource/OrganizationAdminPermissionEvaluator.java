package io.phasetwo.service.resource;

import io.phasetwo.service.model.OrganizationModel;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.permissions.*;

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
      public boolean canManageAuthorization() {
        return realm.canManageAuthorization();
      }

      @Override
      public boolean canManageEvents() {
        return realm.canManageEvents();
      }

      @Override
      public boolean canManageIdentityProviders() {
        // custom
        return (realm.canManageIdentityProviders()
            || auth.hasOrgManageIdentityProviders(organization));
      }

      @Override
      public boolean canManageRealm() {
        return realm.canManageRealm();
      }

      @Override
      public boolean canViewAuthorization() {
        return realm.canViewAuthorization();
      }

      @Override
      public boolean canViewEvents() {
        return realm.canViewEvents();
      }

      @Override
      public boolean canViewIdentityProviders() {
        // custom
        return (realm.canViewIdentityProviders() || auth.hasOrgViewIdentityProviders(organization));
      }

      @Override
      public boolean canViewRealm() {
        return realm.canViewRealm();
      }

      @Override
      public void requireManageAuthorization() {
        realm.requireManageAuthorization();
      }

      @Override
      public void requireManageEvents() {
        realm.requireManageEvents();
      }

      @Override
      public void requireManageIdentityProviders() {
        realm.requireManageIdentityProviders();
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
      public void requireViewAuthorization() {
        realm.requireViewAuthorization();
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
        realm.requireViewIdentityProviders();
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
