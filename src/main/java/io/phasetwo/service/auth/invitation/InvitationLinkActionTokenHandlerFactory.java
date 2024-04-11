package io.phasetwo.service.auth.invitation;

import com.google.auto.service.AutoService;
import org.keycloak.Config;
import org.keycloak.authentication.actiontoken.ActionTokenHandlerFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(ActionTokenHandlerFactory.class)
public class InvitationLinkActionTokenHandlerFactory
    implements ActionTokenHandlerFactory<InvitationLinkActionToken> {

  public static final String PROVIDER_ID = "ext-org-invitation-link";

  @Override
  public void close() {}

  @Override
  public InvitationLinkActionTokenHandler create(KeycloakSession session) {
    return new InvitationLinkActionTokenHandler();
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
