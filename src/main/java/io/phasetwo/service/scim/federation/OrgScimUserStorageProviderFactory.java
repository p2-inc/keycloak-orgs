package io.phasetwo.service.scim.federation;

import com.google.auto.service.AutoService;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.scim.ComponentScimConfig;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * Registers an "Organization SCIM" User Federation provider. Its sole purpose is to make Keycloak's
 * ComponentModel infrastructure accept SCIM-config persistence: the SCIM-tab REST endpoint creates
 * a {@code ComponentModel} with {@code providerType=org.keycloak.storage.UserStorageProvider} and
 * {@code providerId="Organization SCIM"}, and Keycloak validates that combo against a registered
 * factory at add/update time.
 *
 * <p>Side effect: the entry appears in the Keycloak Admin Console's User Federation page. That's
 * accepted as a cosmetic compromise — the canonical UI for managing SCIM configs is the per-org
 * SCIM tab.
 *
 * <p>An earlier iteration tried to gate this factory behind {@code
 * EnvironmentDependentProviderFactory#isSupported} so it could be hidden from the User Federation
 * list by default, but that broke storage entirely (no registered factory means {@code
 * realm.addComponentModel} throws "No such provider"). Hiding just the UI listing without breaking
 * storage would require turning {@code ScimConfigurationProviderFactory} into a full {@link
 * org.keycloak.component.ComponentFactory} and switching the storage providerType to our SPI's
 * FQCN.
 */
@SuppressWarnings("rawtypes")
@JBossLog
@AutoService(UserStorageProviderFactory.class)
public class OrgScimUserStorageProviderFactory
    implements UserStorageProviderFactory<OrgScimUserStorageProvider> {

  public static final String PROVIDER_ID = "Organization SCIM";

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
