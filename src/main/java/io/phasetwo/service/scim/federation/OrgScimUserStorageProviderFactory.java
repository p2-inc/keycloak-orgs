package io.phasetwo.service.scim.federation;

import com.google.auto.service.AutoService;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.scim.ComponentScimConfig;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * Registers an "Organization SCIM" User Federation provider so SCIM
 * configurations can be created directly from the Keycloak Admin
 * Console's User Federation page. This is an alternative entry point to
 * the {@code /{realm}/orgs/{orgId}/scim} REST endpoint / Admin UI tab
 * and exists primarily for power users.
 *
 * <p>Disabled by default. Opt-in via either:
 * <ul>
 *   <li>JVM system property {@code phasetwo.scim.userFederationUi=true}, or
 *   <li>Keycloak SPI config
 *       {@code --spi-storage-organization-scim-user-federation-ui-enabled=true}
 *       (env: {@code KC_SPI_STORAGE_ORGANIZATION_SCIM_USER_FEDERATION_UI_ENABLED=true}).
 * </ul>
 *
 * <p>The {@link EnvironmentDependentProviderFactory#isSupported(Config.Scope)}
 * check is consulted at Keycloak startup; when it returns false the
 * factory is not registered at all and the User Federation page does not
 * list "Organization SCIM".
 */
@SuppressWarnings("rawtypes")
@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class OrgScimUserStorageProviderFactory
    implements UserStorageProviderFactory<OrgScimUserStorageProvider>,
        EnvironmentDependentProviderFactory {

  public static final String PROVIDER_ID = "Organization SCIM";

  public static final String SYS_PROP_USER_FEDERATION_UI = "phasetwo.scim.userFederationUi";
  public static final String SPI_CONFIG_USER_FEDERATION_UI = "userFederationUiEnabled";

  @Override
  public boolean isSupported(Config.Scope config) {
    if (Boolean.parseBoolean(System.getProperty(SYS_PROP_USER_FEDERATION_UI, "false"))) {
      return true;
    }
    return config != null && config.getBoolean(SPI_CONFIG_USER_FEDERATION_UI, false);
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ComponentScimConfig.getConfigProperties();
  }

  @Override
  public OrgScimUserStorageProvider create(KeycloakSession session, ComponentModel model) {
    return new OrgScimUserStorageProvider(session, model);
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getHelpText() {
    return "Organization SCIM v2";
  }

  @Override
  public void validateConfiguration(
      KeycloakSession session, RealmModel realm, ComponentModel model) {
    log.debug("OrgScimUserStorageProviderFactory validateConfiguration");

    // set component ID = organization ID
    ComponentScimConfig config = new ComponentScimConfig(model);
    if (model.getId() == null) {
      // check for another component with the same ID
      ComponentModel otherModel =
          session.getContext().getRealm().getComponent(config.getOrganizationId());
      if (otherModel != null) {
        throw new ComponentValidationException(
            "Another SCIM provider already exists for organization ID: "
                + config.getOrganizationId());
      }
      config.setId(config.getOrganizationId());
    }

    // organization must exist
    OrganizationModel organization =
        session.getProvider(OrganizationProvider.class).getOrganizationById(realm, config.getId());
    if (organization == null) {
      throw new ComponentValidationException(
          "Organization ID not found: " + config.getOrganizationId());
    }
  }

  @Override
  public void onCreate(KeycloakSession session, RealmModel realm, ComponentModel model) {
    log.debug("OrgScimUserStorageProviderFactory onCreate");
  }

  @Override
  public void onUpdate(
      KeycloakSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
    log.debug("OrgScimUserStorageProviderFactory onUpdate");
  }
}
