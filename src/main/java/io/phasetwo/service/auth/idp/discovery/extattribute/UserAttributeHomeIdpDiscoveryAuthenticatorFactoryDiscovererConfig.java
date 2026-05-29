package io.phasetwo.service.auth.idp.discovery.extattribute;

import io.phasetwo.service.auth.idp.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import java.util.List;
import org.keycloak.provider.ProviderConfigProperty;

class UserAttributeHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig
    implements AbstractHomeIdpDiscoveryAuthenticatorFactory.DiscovererConfig {
  @Override
  public List<ProviderConfigProperty> getProperties() {
    return UserAttributeHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
  }

  @Override
  public String getProviderId() {
    return UserAttributeHomeIdpDiscovererFactory.PROVIDER_ID;
  }
}
