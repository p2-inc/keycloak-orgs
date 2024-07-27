package de.sventorben.keycloak.authentication.hidpd;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ServerInfoAwareProviderFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.ALTERNATIVE;
import static org.keycloak.models.AuthenticationExecutionModel.Requirement.DISABLED;
import static org.keycloak.models.AuthenticationExecutionModel.Requirement.REQUIRED;

@AutoService(AuthenticatorFactory.class)
public final class PhaseTwoAuthenticatorFactory implements AuthenticatorFactory, ServerInfoAwareProviderFactory {

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = new AuthenticationExecutionModel.Requirement[]{REQUIRED, ALTERNATIVE, DISABLED};

    private static final String PROVIDER_ID = "ext-auth-home-idp-discovery";

    public Authenticator create(KeycloakSession session) {

        //@xpg -this could be simplified if we could convince the HomeIDPProvider guy to remove final from the creation phase
        // public final Authenticator create(KeycloakSession session) {
        //    return new HomeIdpDiscoveryAuthenticator(discovererConfig);
        // }
        return new PhaseTwoAuthenticator(new AbstractHomeIdpDiscoveryAuthenticatorFactory.DiscovererConfig() {
            public List<ProviderConfigProperty> getProperties() {
                return OrgsEmailHomeIdpDiscovererConfig.CONFIG_PROPERTIES;
            }

            public String getProviderId() {
                return "orgs-ext-email";
            }
        });
    }

    @Override
    public String getDisplayType() {
        return "PhaseTwo Home IdP Discovery";
    }

    @Override
    public String getReferenceCategory() {
        return "Authorization";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public final AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Redirects users to their home identity provider";
    }

    @Override
    public final List<ProviderConfigProperty> getConfigProperties() {
        return Stream.concat(
                        HomeIdpForwarderConfigProperties.CONFIG_PROPERTIES.stream(),
                        OrgsEmailHomeIdpDiscovererConfig.CONFIG_PROPERTIES.stream())
                .collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        return OperationalInfo.get();
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
}

