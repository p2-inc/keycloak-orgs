package io.phasetwo.service.auth.idp.discovery.extattribute;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import org.keycloak.authentication.AuthenticatorFactory;

@AutoService(AuthenticatorFactory.class)
public final class UserAttributeHomeIdpDiscoveryAuthenticatorFactory extends AbstractHomeIdpDiscoveryAuthenticatorFactory implements AuthenticatorFactory {
    private static final String PROVIDER_ID = "ext-user-attribute";

    public UserAttributeHomeIdpDiscoveryAuthenticatorFactory() {
        super(new UserAttributeHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig());
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
