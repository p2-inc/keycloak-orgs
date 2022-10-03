package io.phasetwo.service.auth;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public interface DefaultAuthenticator extends Authenticator {

  default boolean requiresUser() {
    return false;
  }

  default boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
    return true;
  }

  default void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}
}
