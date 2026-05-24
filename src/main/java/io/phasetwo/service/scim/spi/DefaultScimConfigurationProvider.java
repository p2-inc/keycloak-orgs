package io.phasetwo.service.scim.spi;

import io.phasetwo.service.scim.ComponentScimConfig;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/**
 * Default implementation of ScimConfigurationProvider backed by Keycloak ComponentModel.
 */
public class DefaultScimConfigurationProvider implements ScimConfigurationProvider {

  private final KeycloakSession session;

  public DefaultScimConfigurationProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public ComponentScimConfig getConfiguration(String organizationId) {
    RealmModel realm = session.getContext().getRealm();
    ComponentModel component = realm.getComponent(organizationId);
    if (component == null) {
      return null;
    }
    return new ComponentScimConfig(component);
  }

  @Override
  public ComponentScimConfig createConfiguration(
      String organizationId, ComponentScimConfig config) {
    RealmModel realm = session.getContext().getRealm();

    ComponentModel existing = realm.getComponent(organizationId);
    if (existing != null) {
      throw new IllegalStateException(
          "SCIM configuration already exists for organization: " + organizationId);
    }

    ComponentModel model = config.getModel();
    model.setId(organizationId);
    model.setParentId(realm.getId());
    model.setName("Organization SCIM");
    model.setProviderId("Organization SCIM");
    model.setProviderType("org.keycloak.storage.UserStorageProvider");
    model.put(ComponentScimConfig.ORGANIZATION_ID, organizationId);
    realm.addComponentModel(model);

    return getConfiguration(organizationId);
  }

  @Override
  public ComponentScimConfig updateConfiguration(
      String organizationId, ComponentScimConfig config) {
    RealmModel realm = session.getContext().getRealm();

    ComponentModel existing = realm.getComponent(organizationId);
    if (existing == null) {
      throw new IllegalArgumentException(
          "No SCIM configuration found for organization: " + organizationId);
    }

    copyConfigToModel(existing, config);
    realm.updateComponent(existing);

    return getConfiguration(organizationId);
  }

  @Override
  public void deleteConfiguration(String organizationId) {
    RealmModel realm = session.getContext().getRealm();

    ComponentModel existing = realm.getComponent(organizationId);
    if (existing == null) {
      throw new IllegalArgumentException(
          "No SCIM configuration found for organization: " + organizationId);
    }

    realm.removeComponent(existing);
  }

  @Override
  public boolean hasConfiguration(String organizationId) {
    RealmModel realm = session.getContext().getRealm();
    return realm.getComponent(organizationId) != null;
  }

  @Override
  public void close() {}

  private void copyConfigToModel(ComponentModel target, ComponentScimConfig source) {
    if (source.getAuthenticationMode() != null) {
      target.put(
          ComponentScimConfig.SCIM_AUTHENTICATION_MODE,
          source.getAuthenticationMode().name());
    }
    setOrRemove(target, ComponentScimConfig.SCIM_EXTERNAL_ISSUER, source.getExternalIssuer());
    setOrRemove(target, ComponentScimConfig.SCIM_EXTERNAL_AUDIENCE, source.getExternalAudience());
    setOrRemove(target, ComponentScimConfig.SCIM_EXTERNAL_JWKS_URI, source.getExternalJwksUri());
    setOrRemove(
        target, ComponentScimConfig.SCIM_EXTERNAL_SHARED_SECRET, source.getSharedSecret());
    setOrRemove(
        target, ComponentScimConfig.SCIM_BASIC_AUTH_USERNAME, source.getBasicAuthUsername());
    setOrRemove(
        target, ComponentScimConfig.SCIM_BASIC_AUTH_PASSWORD, source.getBasicAuthPassword());
    target.put(ComponentScimConfig.SCIM_LINK_IDP, String.valueOf(source.getLinkIdp()));
    target.put(
        ComponentScimConfig.SCIM_EMAIL_AS_USERNAME, String.valueOf(source.getEmailAsUsername()));
    target.put(ComponentScimConfig.ENABLED_PROPERTY, String.valueOf(source.isEnabled()));
  }

  private void setOrRemove(ComponentModel model, String key, String value) {
    if (value != null) {
      model.put(key, value);
    } else {
      model.getConfig().remove(key);
    }
  }
}
