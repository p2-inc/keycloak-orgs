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
    private final Users users;

    LoginHint(AuthenticationFlowContext context, Users users) {
        this.context = context;
        this.users = users;
    }

    void setInAuthSession(IdentityProviderModel homeIdp, String username) {
        String loginHint = username;
        UserModel user = users.lookupBy(username);
        if (user != null) {
            Map<String, String> idpToUsername = context.getSession().users()
                    .getFederatedIdentitiesStream(context.getRealm(), user)
                    .collect(
                            Collectors.toMap(FederatedIdentityModel::getIdentityProvider,
                                    FederatedIdentityModel::getUserName));
            String alias = homeIdp == null ? "" : homeIdp.getAlias();
            loginHint = idpToUsername.getOrDefault(alias, username);
        }
        setInAuthSession(loginHint);
    }

    void setInAuthSession(String loginHint) {
        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, loginHint);
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