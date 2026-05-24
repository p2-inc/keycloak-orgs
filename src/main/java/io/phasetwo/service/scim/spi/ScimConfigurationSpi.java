package io.phasetwo.service.scim.spi;

import com.google.auto.service.AutoService;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

@AutoService(Spi.class)
public class ScimConfigurationSpi implements Spi {

  @Override
  public boolean isInternal() {
    return false;
  }

  @Override
  public String getName() {
    return "phasetwo-scim-configuration";
  }

  @Override
  public Class<? extends Provider> getProviderClass() {
    return ScimConfigurationProvider.class;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Class<? extends ProviderFactory> getProviderFactoryClass() {
    return ScimConfigurationProviderFactory.class;
  }
}
