//package de.sventorben.keycloak.authentication.hidpd.discovery.email;
package io.phasetwo.service.auth.idp.discovery.extemail;

import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;
import java.util.Optional;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

final class EmailHomeIdpDiscovererConfig {

    private static final String FORWARD_TO_LINKED_IDP = "forwardToLinkedIdp";
    private static final String USER_ATTRIBUTE = "userAttribute";
    private static final String REQUIRE_VERIFIED_EMAIL = "requireVerifiedEmail";
    private static final String REQUIRE_VERIFIED_DOMAIN = "requireVerifiedDomain";

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
        "The user attribute used to lookup the email address of the user.",
        STRING_TYPE,
        "email",
        false);

    private static final ProviderConfigProperty REQUIRE_VERIFIED_EMAIL_PROPERTY =
            new ProviderConfigProperty(
                    REQUIRE_VERIFIED_EMAIL,
                    "Require a verified email",
                    "Whether a verified email address for a user is required to forward to their identity provider.",
                    BOOLEAN_TYPE,
                    false,
                    false);

    private static final ProviderConfigProperty REQUIRE_VERIFIED_DOMAIN_PROPERTY =
            new ProviderConfigProperty(
                    REQUIRE_VERIFIED_DOMAIN,
                    "Require a verified domain",
                    "Whether a verified domain name for an organization is required to forward to their identity provider.",
                    BOOLEAN_TYPE,
                    false,
                    false);



    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
        .property(USER_ATTRIBUTE_PROPERTY)
        .property(FORWARD_TO_LINKED_IDP_PROPERTY)
        .property(REQUIRE_VERIFIED_EMAIL_PROPERTY)
        .property(REQUIRE_VERIFIED_DOMAIN_PROPERTY)
        .build();
    private final AuthenticatorConfigModel authenticatorConfigModel;

    public EmailHomeIdpDiscovererConfig(AuthenticatorConfigModel authenticatorConfigModel) {
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

    boolean requireVerifiedEmail() {
        return Optional.ofNullable(authenticatorConfigModel)
                .map(
                        it ->
                                Boolean.parseBoolean(it.getConfig().getOrDefault(REQUIRE_VERIFIED_EMAIL, "false")))
                .orElse(false);
    }

    boolean requireVerifiedDomain() {
        return Optional.ofNullable(authenticatorConfigModel)
                .map(
                        it ->
                                Boolean.parseBoolean(it.getConfig().getOrDefault(REQUIRE_VERIFIED_DOMAIN, "false")))
                .orElse(false);
    }

    String getAlias() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(AuthenticatorConfigModel::getAlias)
            .orElse("<unconfigured>");
    }
}
