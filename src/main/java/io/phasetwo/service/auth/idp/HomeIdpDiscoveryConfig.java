//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.models.AuthenticatorConfigModel;

import java.util.Optional;

final class HomeIdpDiscoveryConfig {

    static final String REQUIRE_VERIFIED_EMAIL = "requireVerifiedEmail";
    static final String REQUIRE_VERIFIED_DOMAIN = "requireVerifiedDomain";
    static final String FORWARD_TO_LINKED_IDP = "forwardToLinkedIdp";
    static final String BYPASS_LOGIN_PAGE = "bypassLoginPage";
    static final String USER_ATTRIBUTE = "userAttribute";
    static final String FORWARD_TO_FIRST_MATCH = "forwardToFirstMatch";

    private final AuthenticatorConfigModel authenticatorConfigModel;

    HomeIdpDiscoveryConfig(AuthenticatorConfigModel authenticatorConfigModel) {
        this.authenticatorConfigModel = authenticatorConfigModel;
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

    boolean forwardToLinkedIdp() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(FORWARD_TO_LINKED_IDP, "false")))
            .orElse(false);
    }

    boolean bypassLoginPage() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(BYPASS_LOGIN_PAGE, "false")))
            .orElse(false);
    }

    String userAttribute() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> it.getConfig().getOrDefault(USER_ATTRIBUTE, "email").trim())
            .orElse("email");
    }

    boolean forwardToFirstMatch() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(FORWARD_TO_FIRST_MATCH, "true")))
            .orElse(true);
    }
}
