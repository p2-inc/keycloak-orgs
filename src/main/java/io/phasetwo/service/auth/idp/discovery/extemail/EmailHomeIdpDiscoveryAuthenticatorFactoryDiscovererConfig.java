// package de.sventorben.keycloak.authentication.hidpd.discovery.email;
package io.phasetwo.service.auth.idp.discovery.extemail;

import io.phasetwo.service.auth.idp.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import java.util.List;
import org.keycloak.provider.ProviderConfigProperty;

public final class EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig
    implements AbstractHomeIdpDiscoveryAuthenticatorFactory.DiscovererConfig {
  @Override
  public List<ProviderConfigProperty> getProperties() {
    return EmailHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
  }

  @Override
  public String getProviderId() {
    return EmailHomeIdpDiscovererFactory.PROVIDER_ID;
  }
}
