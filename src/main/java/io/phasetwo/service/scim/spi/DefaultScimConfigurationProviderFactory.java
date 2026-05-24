package io.phasetwo.service.scim.spi;

import com.google.auto.service.AutoService;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(ScimConfigurationProviderFactory.class)
public class DefaultScimConfigurationProviderFactory implements ScimConfigurationProviderFactory {

  public static final String PROVIDER_ID = "default";

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public ScimConfigurationProvider create(KeycloakSession session) {
    return new DefaultScimConfigurationProvider(session);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
