package io.phasetwo.service.model.jpa;

import com.google.auto.service.AutoService;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationProviderFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
@AutoService(OrganizationProviderFactory.class)
public class JpaOrganizationProviderFactory implements OrganizationProviderFactory {

  public static final String PROVIDER_ID = "jpa-organization";

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public OrganizationProvider create(KeycloakSession session) {
    EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    return new JpaOrganizationProvider(session, em);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
