//package io.phasetwo.service.auth.idp.discovery.orgs.domainhint;
package io.phasetwo.service.auth.idp.discovery.orgs.domainhint;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.OperationalInfo;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscovererFactory;
import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.Map;

@AutoService(HomeIdpDiscovererFactory.class)
public final class OrgsDomainDiscovererProviderFactory implements HomeIdpDiscovererFactory, EnvironmentDependentProviderFactory, ServerInfoAwareProviderFactory {

    static final String PROVIDER_ID = "orgs-domain";

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION);
    }

    @Override
    public HomeIdpDiscoverer create(KeycloakSession keycloakSession) {
        return new OrgsDomainDiscoverer(keycloakSession);
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public final Map<String, String> getOperationalInfo() {
        return OperationalInfo.get();
    }

}
