//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
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

import static org.keycloak.models.AuthenticationExecutionModel.Requirement.*;

/**
 * Provides a base implementation for authenticator factories that integrate custom identity provider
 * discovery mechanisms within authentication flow of this extension. This abstract class simplifies
 * the creation of authenticator instances by encapsulating common logic and providing a framework
 * for extending the discovery functionality through custom {@link HomeIdpDiscoverer} implementations.
 * <p>
 * Implementors of this class need to provide their own {@link DiscovererConfig}, which includes
 * the discovery logic specifics and configuration properties. This approach ensures flexibility and
 * customizability, enabling developers to tailor the identity provider discovery process to specific
 * organizational needs or authentication scenarios.
 * </p>
 * <p>
 * By inheriting from this class, developers can focus on the specifics of their discovery logic
 * without worrying about the boilerplate associated with UI integration and redirection logic.
 * </p>
 *
 * @apiNote This interface is part of the public API, but is currently unstable and may change in future releases.
 *
 * @see DiscovererConfig
 */
public abstract class AbstractHomeIdpDiscoveryAuthenticatorFactory implements AuthenticatorFactory, ServerInfoAwareProviderFactory {
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = new AuthenticationExecutionModel.Requirement[]{REQUIRED, ALTERNATIVE, DISABLED};

    private final DiscovererConfig discovererConfig;

    protected AbstractHomeIdpDiscoveryAuthenticatorFactory(DiscovererConfig discovererConfig) {
        this.discovererConfig = discovererConfig;
    }

    @Override
    public final boolean isConfigurable() {
        return true;
    }

    @Override
    public final AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public final boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public final List<ProviderConfigProperty> getConfigProperties() {
        return Stream.concat(
                HomeIdpForwarderConfigProperties.CONFIG_PROPERTIES.stream(),
                discovererConfig.getProperties().stream())
            .collect(Collectors.toList());
    }

    @Override
    public final Authenticator create(KeycloakSession session) {
        return new HomeIdpDiscoveryAuthenticator(discovererConfig);
    }

    @Override
    public final void init(Config.Scope config) {
    }

    @Override
    public final void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public final void close() {
    }

    @Override
    public final Map<String, String> getOperationalInfo() {
        return OperationalInfo.get();
    }

    /**
     * Represents the configuration settings for a {@link HomeIdpDiscoverer} implementation. This interface
     * is designed to allow for dynamic specification of configuration properties necessary for the
     * discovery of home Identity Providers (IdPs). The configurations defined by an implementation of
     * this interface provide the parameters and metadata required by a discoverer to properly integrate
     * with {@link HomeIdpDiscoveryAuthenticator}.
     *
     * @apiNote This interface is part of the public API, but is currently unstable and may change in future releases.
     *
     * @see HomeIdpDiscoverer
     */
    @PublicAPI(unstable = true)
    public interface DiscovererConfig {
        /**
         * Retrieves a list of {@link ProviderConfigProperty} objects that define the configuration
         * properties available for the discoverer. Each {@code ProviderConfigProperty} includes metadata
         * such as the property name, type, label, default value, and other attributes necessary for
         * configuring the discoverer identified by {@link #getProviderId()} dynamically at runtime.
         *
         * @return a list of {@link ProviderConfigProperty} that describes each configuration property
         *         required by the discoverer. If no home properties are need for configuration, this method must
         *         return an empty list.
         */
        List<ProviderConfigProperty> getProperties();

        /**
         * Returns the unique provider ID associated with the discoverer. This ID is used to uniquely
         * identify and reference the specific discoverer implementation within the Keycloak system.
         * The provider ID should be unique across all discoverer configurations to prevent conflicts
         * and ensure correct operation.
         *
         * @return the unique string identifier for the discoverer provider.
         */
        String getProviderId();
    }
}
