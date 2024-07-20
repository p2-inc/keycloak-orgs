//package io.phasetwo.service.auth.idp.discovery.orgs.domainhint;
package io.phasetwo.service.auth.idp.discovery.orgs.domainhint;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.AbstractHomeIdpDiscoveryAuthenticatorFactory;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.common.Profile;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

@AutoService(AuthenticatorFactory.class)
public final class OrgsDomainHomeIdpDiscoveryAuthenticatorFactory extends AbstractHomeIdpDiscoveryAuthenticatorFactory implements EnvironmentDependentProviderFactory {
    private static final String PROVIDER_ID = "orgs-domain";

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION);
    }

    public OrgsDomainHomeIdpDiscoveryAuthenticatorFactory() {
        super(new DiscovererConfig() {
            @Override
            public List<ProviderConfigProperty> getProperties() {
                return Collections.emptyList();
            }

            @Override
            public String getProviderId() {
                return OrgsDomainDiscovererProviderFactory.PROVIDER_ID;
            }
        });
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Home IdP Discovery - Organization via Domain Hint";
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their organization's identity provider which will be discovered based on a domain hint";
    }
}
