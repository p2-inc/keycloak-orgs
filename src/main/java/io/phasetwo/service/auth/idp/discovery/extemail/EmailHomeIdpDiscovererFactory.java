package io.phasetwo.service.auth.idp.discovery.extemail;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.OperationalInfo;
import io.phasetwo.service.auth.idp.Users;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscovererFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.Map;

@AutoService(HomeIdpDiscovererFactory.class)
public final class EmailHomeIdpDiscovererFactory implements HomeIdpDiscovererFactory, ServerInfoAwareProviderFactory {

    static final String PROVIDER_ID = "ext-email";

    @Override
    public HomeIdpDiscoverer create(KeycloakSession keycloakSession) {
        return new EmailHomeIdpDiscoverer(new Users(keycloakSession), new DefaultIdentityProviders());
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
    public Map<String, String> getOperationalInfo() {
        return OperationalInfo.get();
    }
}
