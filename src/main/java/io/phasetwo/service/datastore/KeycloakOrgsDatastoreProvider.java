package io.phasetwo.service.datastore;

import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.ExportImportManager;
import org.keycloak.storage.datastore.DefaultDatastoreProvider;

public class KeycloakOrgsDatastoreProvider extends DefaultDatastoreProvider {
  private final KeycloakSession session;

  public KeycloakOrgsDatastoreProvider(
      KeycloakOrgsDatastoreProviderFactory factory, KeycloakSession session) {
    super(factory, session);
    this.session = session;
  }

  @Override
  public ExportImportManager getExportImportManager() {
    return new KeycloakOrgsExportImportManager(session);
  }
}
