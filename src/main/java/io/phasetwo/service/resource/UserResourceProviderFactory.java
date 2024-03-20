package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;
import static io.phasetwo.service.Orgs.KC_ORGS_SKIP_MIGRATION;

import com.google.auto.service.AutoService;
import java.util.HashSet;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.config.UPConfigUtils;

@JBossLog
@AutoService(RealmResourceProviderFactory.class)
public class UserResourceProviderFactory implements RealmResourceProviderFactory {

  static final String ID = "users";

  @Override
  public RealmResourceProvider create(KeycloakSession session) {
    log.debug("UserResourceProviderFactory::create");
    return new UserResourceProvider(session);
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {

    log.debug("UserResourceProviderFactory::postInit");

    factory.register(
        (ProviderEvent event) -> {
          if (event instanceof RealmModel.RealmPostCreateEvent) {
            log.debug("RealmPostCreateEvent");
            realmPostCreateInitUserProfile((RealmModel.RealmPostCreateEvent) event);
          } else if (event instanceof PostMigrationEvent) {
            log.debug("PostMigrationEvent");
            if (KC_ORGS_SKIP_MIGRATION == null) {
              log.info(
                  "initializing active organization user profile attribute following migration");
              KeycloakModelUtils.runJobInTransaction(factory, this::postMigrationInitUserProfile);
            }
          }
        });
  }

  private void realmPostCreateInitUserProfile(RealmModel.RealmPostCreateEvent event) {
    log.debug("UserResourceProviderFactory::realmPostCreateInitUserProfile");
    KeycloakSession session = event.getKeycloakSession();
    // else session.getProvider throw realm is null
    session.getContext().setRealm(event.getCreatedRealm());
    initUserProfile(session);
  }

  private void postMigrationInitUserProfile(KeycloakSession session) {
    log.debug("UserResourceProviderFactory::postMigrationInitUserProfile");
    session
        .realms()
        .getRealmsStream()
        .forEach(
            realm -> {
              session.getContext().setRealm(realm);
              initUserProfile(session);
            });
  }

  private void initUserProfile(KeycloakSession session) {
    log.debug("UserResourceProviderFactory::initUserProfile");

    UPConfig config = session.getProvider(UserProfileProvider.class).getConfiguration();
    addActiveOrganizationAttribute(config);

    UserProfileProvider t = session.getProvider(UserProfileProvider.class);
    try {
      t.setConfiguration(config);
    } catch (ComponentValidationException e) {
      // show validation result containing details about error
      log.error(e.getMessage());
    }
  }

  private void addActiveOrganizationAttribute(UPConfig config) {
    if (config.getAttribute(ACTIVE_ORGANIZATION) != null) return;

    UPAttribute activeOrgAttribute = new UPAttribute();
    activeOrgAttribute.setName(ACTIVE_ORGANIZATION);
    activeOrgAttribute.setDisplayName("Active organization ID");
    UPAttributePermissions permissions = new UPAttributePermissions();
    permissions.setEdit(new HashSet<>(List.of(UPConfigUtils.ROLE_ADMIN)));
    permissions.setView(new HashSet<>(List.of(UPConfigUtils.ROLE_ADMIN)));
    activeOrgAttribute.setPermissions(permissions);
    activeOrgAttribute.setMultivalued(false);
    config.getAttributes().add(activeOrgAttribute);
  }

  @Override
  public void close() {}

  @Override
  public String getId() {
    return ID;
  }
}
