package io.phasetwo.service.auth.idp.discovery.extattribute;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public final class UserAttributeHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig extends AbstractHomeIdpDiscoveryAuthenticatorFactory implements AuthenticatorFactory {
    private static final String PROVIDER_ID = "ext-user-attribute";

    public UserAttributeHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig() {
        super(new DiscovererConfig() {
            @Override
            public List<ProviderConfigProperty> getProperties() {
                return UserAttributeHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
            }

            @Override
            public String getProviderId() {
                return UserAttributeHomeIdpDiscovererFactory.PROVIDER_ID;
            }
        });
    }

    @Override
    public String getDisplayType() {
        return "Home IdP Discovery - Organization via user attribute";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their organization's identity provider which will be discovered based on a user attribute";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
