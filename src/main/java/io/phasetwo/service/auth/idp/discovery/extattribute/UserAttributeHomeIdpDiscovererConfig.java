package io.phasetwo.service.auth.idp.discovery.extattribute;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;
import java.util.Optional;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

final class UserAttributeHomeIdpDiscovererConfig {

    private static final String FORWARD_TO_LINKED_IDP = "forwardToLinkedIdp";
    private static final String USER_ATTRIBUTE = "userAttribute";

    private static final ProviderConfigProperty FORWARD_TO_LINKED_IDP_PROPERTY = new ProviderConfigProperty(
        FORWARD_TO_LINKED_IDP,
        "Forward to linked IdP",
        "Whether to forward existing user to a linked identity provider or not.",
        BOOLEAN_TYPE,
        false,
        false);

    private static final ProviderConfigProperty USER_ATTRIBUTE_PROPERTY = new ProviderConfigProperty(
        USER_ATTRIBUTE,
        "User attribute",
        "The user attribute used to match the identity provider.",
        STRING_TYPE,
        "id",
        false);


    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property(USER_ATTRIBUTE_PROPERTY)
        .property(FORWARD_TO_LINKED_IDP_PROPERTY)
        .build();
    private final AuthenticatorConfigModel authenticatorConfigModel;

    public UserAttributeHomeIdpDiscovererConfig(AuthenticatorConfigModel authenticatorConfigModel) {
        this.authenticatorConfigModel = authenticatorConfigModel;
    }

    boolean forwardToLinkedIdp() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(FORWARD_TO_LINKED_IDP, "false")))
            .orElse(false);
    }

    String userAttribute() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> it.getConfig().getOrDefault(USER_ATTRIBUTE, "email").trim())
            .orElse("email");
    }

    String getAlias() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(AuthenticatorConfigModel::getAlias)
            .orElse("<unconfigured>");
    }
}
