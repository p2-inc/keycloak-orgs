package io.phasetwo.service.auth.idp;

import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;

public final class Users {

    private static final Logger LOG = Logger.getLogger(Users.class);

    private final KeycloakSession keycloakSession;

    public Users(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    public UserModel lookupBy(String username) {
        UserModel user = null;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(keycloakSession, keycloakSession.getContext().getRealm(), username);
        } catch (ModelDuplicateException ex) {
            LOG.warnf(ex, "Could not uniquely identify the user. Multiple users with name or email '%s' found.", username);
        }
        return user;
    }

}
