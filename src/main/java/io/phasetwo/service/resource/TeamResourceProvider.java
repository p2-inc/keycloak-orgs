package io.phasetwo.service.resource;

import org.keycloak.models.KeycloakSession;

public class TeamResourceProvider extends BaseRealmResourceProvider {

  public TeamResourceProvider(KeycloakSession session) {
    super(session);
  }

  @Override
  protected Object getRealmResource() {
    TeamResource team = new TeamResource(session);
    team.setup();
    return team;
  }
}
