package io.phasetwo.service.auth.invitation;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/** */
@JBossLog
@AutoService(RequiredActionFactory.class)
public class InvitationRequiredActionFactory implements RequiredActionFactory {

  public static final String PROVIDER_ID = "invitation-required-action";

  @Override
  public RequiredActionProvider create(KeycloakSession session) {
    return new InvitationRequiredAction();
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayText() {
    return "Invitation";
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
