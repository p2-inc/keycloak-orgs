package de.sventorben.keycloak.authentication.hidpd;

import com.google.auto.service.AutoService;
import de.sventorben.keycloak.authentication.hidpd.discovery.email.EmailHomeIdpDiscoverer;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscoverer;
import de.sventorben.keycloak.authentication.hidpd.discovery.spi.HomeIdpDiscovererFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.Map;

@AutoService(HomeIdpDiscovererFactory.class)
public final class OrgsEmailHomeIdpDiscovererFactory implements HomeIdpDiscovererFactory, ServerInfoAwareProviderFactory {

    static final String PROVIDER_ID = "orgs-ext-email";

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
