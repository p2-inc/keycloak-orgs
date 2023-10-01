package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.OrganizationAdminAuth.DEFAULT_ORG_ROLES;
import static io.phasetwo.service.resource.OrganizationAdminAuth.ROLE_CREATE_ORGANIZATION;
import static io.phasetwo.service.resource.OrganizationAdminAuth.ROLE_MANAGE_ORGANIZATION;
import static io.phasetwo.service.resource.OrganizationAdminAuth.ROLE_VIEW_ORGANIZATION;

import com.google.auto.service.AutoService;
import io.phasetwo.service.Orgs;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/** */
@JBossLog
@AutoService(RealmResourceProviderFactory.class)
public class OrganizationResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "orgs";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public void close() {}

  @Override
  public OrganizationResourceProvider create(KeycloakSession session) {
    log.debug("OrganizationResourceProviderFactory::create");
    return new OrganizationResourceProvider(session);
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {

    log.debug("OrganizationResourceProviderFactory::postInit");

    factory.register(
        (ProviderEvent event) -> {
          if (event instanceof RealmModel.RealmPostCreateEvent) {
            log.debug("RealmPostCreateEvent");
            realmPostCreate((RealmModel.RealmPostCreateEvent) event);
          } else if (event instanceof PostMigrationEvent) {
            log.debug("PostMigrationEvent");
            KeycloakModelUtils.runJobInTransaction(factory, this::initRoles);
          } else if (event instanceof RealmModel.RealmRemovedEvent) {
            log.debug("RealmRemovedEvent");
            realmRemoved((RealmModel.RealmRemovedEvent) event);
          } else if (event instanceof UserModel.UserRemovedEvent) {
            log.debug("UserRemovedEvent");
            userRemoved((UserModel.UserRemovedEvent) event);
          } else if (event instanceof OrganizationModel.OrganizationCreationEvent) {
            log.debug("OrganizationCreationEvent");
            organizationCreation((OrganizationModel.OrganizationCreationEvent) event);
          } else if (event instanceof OrganizationModel.OrganizationRemovedEvent) {
            log.debug("OrganizationRemovedEvent");
            organizationRemoved((OrganizationModel.OrganizationRemovedEvent) event);
          }
        });
  }

  private void initRoles(KeycloakSession session) {

    log.debug("OrganizationResourceProviderFactory::initRoles");

    RealmManager manager = new RealmManager(session);
    session
        .realms()
        .getRealmsStream()
        .forEach(
            realm -> {
              ClientModel client = realm.getMasterAdminClient();
              if (client.getRole(ROLE_VIEW_ORGANIZATION) == null
                  || client.getRole(ROLE_MANAGE_ORGANIZATION) == null
                  || client.getRole(ROLE_CREATE_ORGANIZATION) == null) {
                addMasterAdminRoles(manager, realm);
              }
              if (!realm.getName().equals(Config.getAdminRealm())) {
                client = realm.getClientByClientId(manager.getRealmAdminClientId(realm));
                if (client.getRole(ROLE_VIEW_ORGANIZATION) == null
                    || client.getRole(ROLE_MANAGE_ORGANIZATION) == null
                    || client.getRole(ROLE_CREATE_ORGANIZATION) == null) {
                  addRealmAdminRoles(manager, realm);
                }
              }
            });
  }

  private void realmPostCreate(RealmModel.RealmPostCreateEvent event) {
    RealmModel realm = event.getCreatedRealm();
    RealmManager manager = new RealmManager(event.getKeycloakSession());
    addMasterAdminRoles(manager, realm);
    if (!realm.getName().equals(Config.getAdminRealm())) addRealmAdminRoles(manager, realm);
  }

  private void addMasterAdminRoles(RealmManager manager, RealmModel realm) {

    RealmModel master = manager.getRealmByName(Config.getAdminRealm());
    RoleModel admin = master.getRole(AdminRoles.ADMIN);
    ClientModel client = realm.getMasterAdminClient();

    addRoles(client, admin);
  }

  private void addRealmAdminRoles(RealmManager manager, RealmModel realm) {

    ClientModel client = realm.getClientByClientId(manager.getRealmAdminClientId(realm));
    RoleModel admin = client.getRole(AdminRoles.REALM_ADMIN);

    addRoles(client, admin);
  }

  private void addRoles(ClientModel client, RoleModel parent) {

    String[] names = new String[] {ROLE_VIEW_ORGANIZATION, ROLE_MANAGE_ORGANIZATION};

    for (String name : names) {
      addRole(name, client, parent, true);
    }

    addRole(ROLE_CREATE_ORGANIZATION, client, parent, false);
  }

  private void addRole(String name, ClientModel client, RoleModel parent, boolean composite) {
    if (client.getRole(name) == null) {
      RoleModel role = client.addRole(name);
      role.setDescription("${role_" + name + "}");
      if (composite) parent.addCompositeRole(role);
    }
  }

  private void realmRemoved(RealmModel.RealmRemovedEvent event) {
    event
        .getKeycloakSession()
        .getProvider(OrganizationProvider.class)
        .removeOrganizations(event.getRealm());
  }

  private void userRemoved(UserModel.UserRemovedEvent event) {
    OrganizationProvider orgs = event.getKeycloakSession().getProvider(OrganizationProvider.class);
    orgs.getUserOrganizationsStream(event.getRealm(), event.getUser())
        .forEach(
            org -> {
              try {
                org.revokeMembership(event.getUser());
                org.getRolesStream()
                    .forEach(
                        r -> {
                          if (r.hasRole(event.getUser())) r.revokeRole(event.getUser());
                        });
              } catch (Exception e) {
                log.warn("error removing user from org", e);
              }
            });
  }

  private void organizationCreation(OrganizationModel.OrganizationCreationEvent event) {
    OrganizationModel org = event.getOrganization();

    // setup default roles
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      org.addRole(role);
    }

    // create default admin user
    String adminUsername = getDefaultAdminUsername(org);
    UserModel user =
        event
            .getKeycloakSession()
            .users()
            .addUser(event.getRealm(), KeycloakModelUtils.generateId(), adminUsername, true, false);
    user.setEnabled(true);
    // other defaults? email? emailVerified? attributes?
    user.setEmail(String.format("%s@noreply.phasetwo.io", adminUsername)); // todo dynamic email?
    user.setEmailVerified(true);
    org.grantMembership(user);
    for (String role : DEFAULT_ORG_ROLES) {
      OrganizationRoleModel roleModel = org.getRoleByName(role);
      roleModel.grantRole(user);
    }
  }

  private void organizationRemoved(OrganizationModel.OrganizationRemovedEvent event) {
    // TODO anything else to do? does cascade take care of it?

    // remove the idp associations for this org
    OrganizationModel org = event.getOrganization();
    try {
      org.getIdentityProvidersStream()
          .forEach(
              idp -> {
                idp.getConfig().remove(Orgs.ORG_OWNER_CONFIG_KEY);
              });
    } catch (Exception e) {
      log.warnf(
          "Couldn't remove identity providers on organizationRemoved. Likely because this follows a realmRemoved event. %s",
          e.getMessage());
    }

    // delete default admin user
    try {
      UserModel user =
          event
              .getKeycloakSession()
              .users()
              .getUserByUsername(
                  event.getRealm(), getDefaultAdminUsername(event.getOrganization()));
      if (user != null) {
        boolean removed = event.getKeycloakSession().users().removeUser(event.getRealm(), user);
        log.debugf(
            "User removed on deletion of org %s? %b", event.getOrganization().getId(), removed);
      } else {
        log.warnf(
            "Default org admin %s for org %s doesn't exist. Skipping deletion on org removal.",
            getDefaultAdminUsername(event.getOrganization()), event.getOrganization().getId());
      }
    } catch (Exception e) {
      log.warnf(
          "Couldn't remove default org admin user on organizationRemoved. Likely because this follows a realmRemoved event. %s",
          e.getMessage());
    }
  }

  public static String getDefaultAdminUsername(OrganizationModel org) {
    return String.format("org-admin-%s", org.getId());
  }
}
