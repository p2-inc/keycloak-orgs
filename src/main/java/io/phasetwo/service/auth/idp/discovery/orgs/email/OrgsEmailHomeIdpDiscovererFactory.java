//package io.phasetwo.service.auth.idp.discovery.orgs.email;
package io.phasetwo.service.auth.idp.discovery.orgs.email;

import com.google.auto.service.AutoService;
import io.phasetwo.service.auth.idp.OperationalInfo;
import io.phasetwo.service.auth.idp.Users;
import io.phasetwo.service.auth.idp.discovery.email.EmailHomeIdpDiscoverer;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.Map;
import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscovererFactory;

@AutoService(HomeIdpDiscovererFactory.class)
public final class OrgsEmailHomeIdpDiscovererFactory implements HomeIdpDiscovererFactory, ServerInfoAwareProviderFactory {

    static final String PROVIDER_ID = "orgs-email";

    @Override
    public HomeIdpDiscoverer create(KeycloakSession keycloakSession) {
        return new EmailHomeIdpDiscoverer(new Users(keycloakSession), new OrgsIdentityProviders());
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
