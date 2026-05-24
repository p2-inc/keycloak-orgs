package io.phasetwo.service.scim.federation;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProvider;

/**
 * Empty UserStorageProvider whose only purpose is to make
 * {@link OrgScimUserStorageProviderFactory} discoverable from the Keycloak
 * Admin Console "User Federation" page. That GUI is how operators can
 * configure per-organization SCIM settings when the
 * `phasetwo.scim.userFederationUi=true` system property (or the equivalent
 * SPI config flag) is set. By default the factory is hidden, so this
 * provider is never instantiated.
 *
 * <p>Per-organization SCIM configuration is normally managed through the
 * {@code /{realm}/orgs/{orgId}/scim} REST endpoint and the Admin UI tab
 * exposed by keycloak-themes, both backed by the
 * {@link io.phasetwo.service.scim.spi.ScimConfigurationProvider} SPI.
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
