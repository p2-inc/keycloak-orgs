//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.events.Details;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.AuthenticationManager;

final class RememberMe {

    private final AuthenticationFlowContext context;

    public RememberMe(AuthenticationFlowContext context) {
        this.context = context;
    }

    void remember(String username) {
        String rememberMe = context.getAuthenticationSession().getAuthNote(Details.REMEMBER_ME);
        RealmModel realm = context.getRealm();
        boolean remember = realm.isRememberMe() && "true".equalsIgnoreCase(rememberMe);
        if (remember) {
            AuthenticationManager.createRememberMeCookie(username, context.getUriInfo(), context.getSession());
        } else {
            AuthenticationManager.expireRememberMeCookie(realm, context.getUriInfo(), context.getSession());
        }
    }

    /*
     * Sets session notes for interoperability with other authenticators and Keycloak defaults
     */
    void handleAction(MultivaluedMap<String, String> formData) {
        boolean remember = context.getRealm().isRememberMe() &&
            "on".equalsIgnoreCase(formData.getFirst("rememberMe"));
        if (remember) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
        }
    }

    String getUserName() {
        return AuthenticationManager.getRememberMeUsername(context.getRealm(),
            context.getHttpRequest().getHttpHeaders());
    }
}
