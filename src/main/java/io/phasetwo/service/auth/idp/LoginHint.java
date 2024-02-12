//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.Map;
import java.util.stream.Collectors;

import static org.keycloak.protocol.oidc.OIDCLoginProtocol.LOGIN_HINT_PARAM;

final class LoginHint {

    private final AuthenticationFlowContext context;

    LoginHint(AuthenticationFlowContext context) {
        this.context = context;
    }

    void setInAuthSession(IdentityProviderModel homeIdp, String defaultUsername) {
        if (homeIdp == null) {
            return;
        }
        String loginHint;
        UserModel user = context.getUser();
        if (user != null) {
            Map<String, String> idpToUsername = context.getSession().users()
                .getFederatedIdentitiesStream(context.getRealm(), user)
                .collect(
                    Collectors.toMap(FederatedIdentityModel::getIdentityProvider,
                        FederatedIdentityModel::getUserName));
            loginHint = idpToUsername.getOrDefault(homeIdp.getAlias(), defaultUsername);
            context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
        }
    }

    String getFromSession() {
        return context.getAuthenticationSession().getClientNote(LOGIN_HINT_PARAM);
    }

    void copyTo(ClientSessionCode<AuthenticationSessionModel> clientSessionCode) {
        String loginHint = getFromSession();
        if (clientSessionCode.getClientSession() != null && loginHint != null) {
            clientSessionCode.getClientSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
        }
    }
}
