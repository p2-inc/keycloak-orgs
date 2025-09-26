package io.phasetwo.service.auth.idp.discovery.extemail;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;


public final class EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig implements AbstractHomeIdpDiscoveryAuthenticatorFactory.DiscovererConfig {
    @Override
    public List<ProviderConfigProperty> getProperties() {
        return EmailHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
    }

    @Override
    public String getProviderId() {
        return EmailHomeIdpDiscovererFactory.PROVIDER_ID;
    }

}
