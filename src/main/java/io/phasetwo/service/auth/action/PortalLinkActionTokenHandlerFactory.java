package io.phasetwo.service.auth.action;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.actiontoken.ActionTokenHandlerFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(ActionTokenHandlerFactory.class)
public class PortalLinkActionTokenHandlerFactory
    implements ActionTokenHandlerFactory<PortalLinkActionToken> {

  public static final String PROVIDER_ID = "org-portal-link";

  @Override
  public void close() {}

  @Override
  public PortalLinkActionTokenHandler create(KeycloakSession session) {
    return new PortalLinkActionTokenHandler();
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}
}
