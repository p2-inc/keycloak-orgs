//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.services.managers.AuthenticationManager;

import java.util.List;
import java.util.Optional;

final class AuthenticationChallenge {

    private final AuthenticationFlowContext context;
    private final RememberMe rememberMe;
    private final LoginHint loginHint;
    private final LoginForm loginForm;
    private final Reauthentication reauthentication;

    AuthenticationChallenge(AuthenticationFlowContext context, RememberMe rememberMe, LoginHint loginHint, LoginForm loginForm, Reauthentication reauthentication) {
        this.context = context;
        this.rememberMe = rememberMe;
        this.loginHint = loginHint;
        this.loginForm = loginForm;
        this.reauthentication = reauthentication;
    }

    void forceChallenge() {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        String loginHintUsername = loginHint.getFromSession();

        String rememberMeUsername = rememberMe.getUserName();

        Response challengeResponse;
        if (reauthentication.required() && context.getUser() != null) {
            String attribute = Optional.ofNullable(context.getAuthenticatorConfig())
                .map(it -> it.getConfig().getOrDefault("userAttribute", "email").trim())
                .orElse("email");
            formData.add(AuthenticationManager.FORM_USERNAME, context.getUser().getFirstAttribute(attribute));
            challengeResponse = loginForm.createWithSignInButtonOnly(formData);
        } else {
            if (loginHintUsername != null || rememberMeUsername != null) {
                if (loginHintUsername != null) {
                    formData.add(AuthenticationManager.FORM_USERNAME, loginHintUsername);
                } else {
                    formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                    formData.add("rememberMe", "on");
                }
            }
            challengeResponse = loginForm.create(formData);
        }

        context.challenge(challengeResponse);
    }

    void forceChallenge(List<IdentityProviderModel> homeIdps) {
        context.forceChallenge(loginForm.create(homeIdps));
    }

}
