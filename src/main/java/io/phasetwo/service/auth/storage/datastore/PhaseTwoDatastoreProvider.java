package io.phasetwo.service.auth.storage.datastore;

import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.ExportImportManager;
import org.keycloak.storage.datastore.DefaultDatastoreProvider;

public class PhaseTwoDatastoreProvider extends DefaultDatastoreProvider {
    private final KeycloakSession session;

    public PhaseTwoDatastoreProvider(PhaseTwoDatastoreProviderFactory factory, KeycloakSession session) {
        super(factory, session);
        this.session = session;
    }

    @Override
    public ExportImportManager getExportImportManager() {
        return new PhaseTwoExportImportManager(session);
    }
}
