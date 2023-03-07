package io.phasetwo.service.resource;

import org.keycloak.models.KeycloakSession;

public class UserResourceProvider extends BaseRealmResourceProvider {

  public UserResourceProvider(KeycloakSession session) {
    super(session);
  }

  @Override
  protected Object getRealmResource() {
    UserResource user = new UserResource(session);
    user.setup();
    return user;
  }
}
