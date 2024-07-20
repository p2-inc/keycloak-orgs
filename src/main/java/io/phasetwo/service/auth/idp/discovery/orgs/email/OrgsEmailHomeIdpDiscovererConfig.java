//package io.phasetwo.service.auth.idp.discovery.orgs.email;
package io.phasetwo.service.auth.idp.discovery.orgs.email;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;
import java.util.Optional;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

final class OrgsEmailHomeIdpDiscovererConfig {

    private static final String FORWARD_UNVERIFIED_ATTRIBUTE = "forwardUnverifiedEmail";

    private static final ProviderConfigProperty FORWARD_UNVERIFIED_PROPERTY = new ProviderConfigProperty(
        FORWARD_UNVERIFIED_ATTRIBUTE,
        "Forward users with unverified email",
        "If 'User attribute' is set to 'email', whether to forward existing user if user's email is not verified.",
        BOOLEAN_TYPE,
        false,
        false);

    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property(FORWARD_UNVERIFIED_PROPERTY)
        .build();
    private final AuthenticatorConfigModel authenticatorConfigModel;

    public OrgsEmailHomeIdpDiscovererConfig(AuthenticatorConfigModel authenticatorConfigModel) {
        this.authenticatorConfigModel = authenticatorConfigModel;
    }

    boolean forwardUserWithUnverifiedEmail() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(FORWARD_UNVERIFIED_ATTRIBUTE, "false")))
            .orElse(false);
    }

}
