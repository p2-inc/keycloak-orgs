//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.services.managers.AuthenticationManager;

import java.util.List;

final class AuthenticationChallenge {

    private final AuthenticationFlowContext context;
    private final RememberMe rememberMe;
    private final LoginHint loginHint;
    private final LoginForm loginForm;

    AuthenticationChallenge(AuthenticationFlowContext context, RememberMe rememberMe, LoginHint loginHint, LoginForm loginForm) {
        this.context = context;
        this.rememberMe = rememberMe;
        this.loginHint = loginHint;
        this.loginForm = loginForm;
    }

    void forceChallenge() {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        String loginHintUsername = loginHint.getFromSession();

        String rememberMeUsername = rememberMe.getUserName();

        if (loginHintUsername != null || rememberMeUsername != null) {
            if (loginHintUsername != null) {
                formData.add(AuthenticationManager.FORM_USERNAME, loginHintUsername);
            } else {
                formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                formData.add("rememberMe", "on");
            }
        }
        Response challengeResponse = loginForm.create(formData);
        context.challenge(challengeResponse);
    }

    void forceChallenge(List<IdentityProviderModel> homeIdps) {
        context.forceChallenge(loginForm.create(homeIdps));
    }

}
