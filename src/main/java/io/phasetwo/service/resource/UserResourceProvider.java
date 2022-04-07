package io.phasetwo.service.resource;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class UserResourceProvider extends BaseRealmResourceProvider {

  public UserResourceProvider(KeycloakSession session) {
    super(session);
  }

  @Override
  protected Object getRealmResource() {
    RealmModel realm = session.getContext().getRealm();
    UserResource user = new UserResource(realm);
    ResteasyProviderFactory.getInstance().injectProperties(user);
    user.setup();
    return user;
  }
}
