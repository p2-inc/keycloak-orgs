package io.phasetwo.service.resource;

import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class TeamResourceProvider extends BaseRealmResourceProvider {

  public TeamResourceProvider(KeycloakSession session) {
    super(session);
  }

  @Override
  protected Object getRealmResource() {
    RealmModel realm = session.getContext().getRealm();
    TeamResource team = new TeamResource(realm);
    ResteasyProviderFactory.getInstance().injectProperties(team);
    team.setup();
    return team;
  }
}
