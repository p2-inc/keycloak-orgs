package io.phasetwo.service.auth.idp;

import org.keycloak.models.AuthenticatorConfigModel;

import java.util.Optional;

final class HomeIdpForwarderConfig {

    static final String BYPASS_LOGIN_PAGE = "bypassLoginPage";
    static final String FORWARD_TO_FIRST_MATCH = "forwardToFirstMatch";

    private final AuthenticatorConfigModel authenticatorConfigModel;

    HomeIdpForwarderConfig(AuthenticatorConfigModel authenticatorConfigModel) {
        this.authenticatorConfigModel = authenticatorConfigModel;
    }

    boolean bypassLoginPage() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(BYPASS_LOGIN_PAGE, "false")))
            .orElse(false);
    }

    boolean forwardToFirstMatch() {
        return Optional.ofNullable(authenticatorConfigModel)
            .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(FORWARD_TO_FIRST_MATCH, "true")))
            .orElse(true);
    }
}
