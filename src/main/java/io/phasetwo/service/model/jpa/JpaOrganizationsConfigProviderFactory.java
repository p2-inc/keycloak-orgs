package io.phasetwo.service.model.jpa;

import com.google.auto.service.AutoService;
import io.phasetwo.service.model.OrganizationsConfigProvider;
import io.phasetwo.service.model.OrganizationsConfigProviderFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@JBossLog
@AutoService(OrganizationsConfigProviderFactory.class)
public class JpaOrganizationsConfigProviderFactory implements OrganizationsConfigProviderFactory {

  public static final String PROVIDER_ID = "jpa-organizations-config";

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public OrganizationsConfigProvider create(KeycloakSession session) {
    EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    return new JpaOrganizationsConfigProvider(session, em);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
