package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.DomainModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.jpa.entity.DomainEntity;
import jakarta.persistence.EntityManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.JpaModel;

public class DomainAdapter implements DomainModel, JpaModel<DomainEntity> {

  protected final KeycloakSession session;
  protected final DomainEntity domain;
  protected final EntityManager em;
  protected final RealmModel realm;

  public DomainAdapter(
      KeycloakSession session, RealmModel realm, EntityManager em, DomainEntity domain) {
    this.session = session;
    this.em = em;
    this.domain = domain;
    this.realm = realm;
  }

  @Override
  public DomainEntity getEntity() {
    return domain;
  }

  @Override
  public OrganizationModel getOrganization() {
    return session
        .getProvider(OrganizationProvider.class)
        .getOrganizationById(realm, domain.getOrganization().getId());
  }

  @Override
  public String getDomain() {
    return domain.getDomain();
  }

  @Override
  public boolean isVerified() {
    return domain.isVerified();
  }

  @Override
  public void setVerified(boolean verified) {
    domain.setVerified(verified);
  }
}
