package io.phasetwo.service.model.jpa.entity;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;
import org.keycloak.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/** */
@JBossLog
@AutoService(JpaEntityProviderFactory.class)
public class OrganizationEntityProviderFactory implements JpaEntityProviderFactory {

  protected static final String ID = "organization-entity-provider";

  @Override
  public JpaEntityProvider create(KeycloakSession session) {
    log.debug("OrganizationEntityProviderFactory::create");
    return new OrganizationEntityProvider();
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
