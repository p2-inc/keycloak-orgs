package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.*;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class SuccessAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-success";

  public SuccessAuthenticatorFactory() {
    super(PROVIDER_ID);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.info("SuccessAuthenticatorFactory.authenticate");
    context.success();
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.info("SuccessAuthenticatorFactory.action");
    context.success();
  }

  @Override
  public boolean requiresUser() {
    return false;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return this;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Always returns success. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Always success";
  }

  @Override
  public String getReferenceCategory() {
    return "Debug";
  }
}
