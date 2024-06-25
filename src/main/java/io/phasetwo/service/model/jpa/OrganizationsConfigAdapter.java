package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.OrganizationsConfigModel;
import io.phasetwo.service.model.jpa.entity.OrganizationsConfigEntity;
import jakarta.persistence.EntityManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.JpaModel;

public class OrganizationsConfigAdapter implements OrganizationsConfigModel, JpaModel<OrganizationsConfigEntity> {

    protected final KeycloakSession session;
    protected final OrganizationsConfigEntity config;
    protected final EntityManager em;
    protected final RealmModel realm;

    public OrganizationsConfigAdapter(
            KeycloakSession session, RealmModel realm, EntityManager em, OrganizationsConfigEntity config) {
        this.session = session;
        this.em = em;
        this.config = config;
        this.realm = realm;
    }

    @Override
    public String getId() {
        return config.getId();
    }

    @Override
    public boolean isCreateAdminUser() {
        return config.isCreateAdminUser();
    }

    @Override
    public boolean isSharedIdps() {
        return config.isSharedIdps();
    }

    @Override
    public void setCreateAdminUser(boolean createAdminUser) {
        config.setCreateAdminUser(createAdminUser);
    }

    @Override
    public void setSharedIdps(boolean sharedIdps) {
        config.setSharedIdps(sharedIdps);
    }


    @Override
    public RealmModel getRealm() {
        return session.realms().getRealm(config.getRealmId());
    }


    @Override
    public OrganizationsConfigEntity getEntity() {
        return config;
    }
}
