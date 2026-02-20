//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.discovery.extemail.EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig;
import org.keycloak.authentication.AuthenticatorFactory;

@AutoService(AuthenticatorFactory.class)
public final class HomeIdpDiscoveryAuthenticatorFactory extends AbstractHomeIdpDiscoveryAuthenticatorFactory implements  AuthenticatorFactory {

    private static final String PROVIDER_ID = "ext-auth-home-idp-discovery";

    public HomeIdpDiscoveryAuthenticatorFactory() {
        super(new EmailHomeIdpDiscoveryAuthenticatorFactoryDiscovererConfig());
    }

    @Override
    public String getDisplayType() {
        return "Home IdP Discovery";
    }

    @Override
    public String getReferenceCategory() {
        return "Authorization";
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their home identity provider";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
