package io.phasetwo.service.scim.federation;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProvider;

/**
 * Empty UserStorageProvider. Its existence makes
 * {@link OrgScimUserStorageProviderFactory} a valid registered
 * {@link org.keycloak.storage.UserStorageProviderFactory}, which is
 * what Keycloak's {@code realm.addComponentModel} requires to accept
 * the {@code ComponentModel}s used for per-organization SCIM
 * configuration storage.
 *
 * <p>This provider itself does nothing at runtime; the SCIM config
 * data is read/written by
 * {@link io.phasetwo.service.scim.spi.DefaultScimConfigurationProvider}
 * directly against the realm's components container. Per-organization
 * SCIM configuration is normally managed through the
 * {@code /{realm}/orgs/{orgId}/scim} REST endpoint and the Admin UI
 * tab exposed by keycloak-themes.
 */
public class OrgScimUserStorageProvider implements UserStorageProvider {

  private final KeycloakSession session;
  private final ComponentModel model;

  public OrgScimUserStorageProvider(KeycloakSession session, ComponentModel model) {
    this.session = session;
    this.model = model;
  }

  @Override
  public void close() {}
}
