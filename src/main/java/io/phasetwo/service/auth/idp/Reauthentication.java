//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

final class Reauthentication {

    private final AuthenticationFlowContext context;

    Reauthentication(AuthenticationFlowContext context) {
        this.context = context;
    }

    boolean required() {
        AuthenticationManager.AuthResult authResult = AuthenticationManager.authenticateIdentityCookie(context.getSession(), context.getRealm(), true);
        UserSessionModel userSessionModel = null;
        if (authResult != null) {
            userSessionModel = authResult.session();
        }
        LoginProtocol protocol = context.getSession().getProvider(LoginProtocol.class, context.getAuthenticationSession().getProtocol());
        return protocol.requireReauthentication(userSessionModel, context.getAuthenticationSession());
    }
}
