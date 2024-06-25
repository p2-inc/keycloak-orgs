package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.OrganizationsConfigModel;
import io.phasetwo.service.model.OrganizationsConfigProvider;
import io.phasetwo.service.model.jpa.entity.OrganizationsConfigEntity;
import io.phasetwo.service.representation.OrganizationsConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;

@JBossLog
public class JpaOrganizationsConfigProvider implements OrganizationsConfigProvider {

    protected final KeycloakSession session;
    protected final EntityManager em;

    public JpaOrganizationsConfigProvider(KeycloakSession session, EntityManager em) {
        this.session = session;
        this.em = em;
    }

    @Override
    public OrganizationsConfigModel addConfig(RealmModel realm,
                                              OrganizationsConfig config) {
        OrganizationsConfigEntity entity = new OrganizationsConfigEntity();
        entity.setId(KeycloakModelUtils.generateId());
        entity.setRealmId(realm.getId());
        entity.setCreateAdminUser(config.isCreateAdminUser());
        entity.setSharedIdps(config.isSharedIdps());

        em.persist(entity);
        em.flush();
        return new OrganizationsConfigAdapter(session, realm, em, entity);
    }

    @Override
    public OrganizationsConfigModel getConfig(RealmModel realm) {
        TypedQuery<OrganizationsConfigEntity> query =
                em.createNamedQuery("getConfigsByRealm", OrganizationsConfigEntity.class);
        query.setParameter("realmId", realm.getId());
        return query.getResultStream()
                .findFirst()
                .map(entity -> new OrganizationsConfigAdapter(session, realm, em, entity))
                .orElse(null);
    }

    public OrganizationsConfigAdapter update(RealmModel realm, OrganizationsConfigModel existingConfig) {
        OrganizationsConfigEntity entity = new OrganizationsConfigEntity();
        entity.setId(existingConfig.getId());
        entity.setRealmId(existingConfig.getRealm().getId());
        entity.setCreateAdminUser(existingConfig.isCreateAdminUser());
        entity.setSharedIdps(existingConfig.isSharedIdps());

        em.merge(entity);
        em.flush();

        return new OrganizationsConfigAdapter(session, realm, em, entity);
    }

    @Override
    public void close() {

    }
}
