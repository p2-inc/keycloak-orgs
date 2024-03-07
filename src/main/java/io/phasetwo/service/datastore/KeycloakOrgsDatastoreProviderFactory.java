package io.phasetwo.service.datastore;

import com.google.auto.service.AutoService;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.DatastoreProvider;
import org.keycloak.storage.DatastoreProviderFactory;
import org.keycloak.storage.datastore.DefaultDatastoreProviderFactory;

@AutoService(DatastoreProviderFactory.class)
public class KeycloakOrgsDatastoreProviderFactory extends DefaultDatastoreProviderFactory {

  @Override
  public DatastoreProvider create(KeycloakSession session) {
    return new KeycloakOrgsDatastoreProvider(this, session);
  }
}
