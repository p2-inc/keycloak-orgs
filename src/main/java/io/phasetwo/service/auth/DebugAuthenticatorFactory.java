package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.*;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class DebugAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-debugger";

  public DebugAuthenticatorFactory() {
    super(PROVIDER_ID);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.infof("DebugAuthenticator.authenticate: %s", context.getRealm().getName());
    debug(context);
    if (context.getExecution().getRequirement()
        == AuthenticationExecutionModel.Requirement.REQUIRED) {
      action(context);
    } else {
      context.attempted();
    }
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.infof("DebugAuthenticator.action: %s", context.getRealm().getName());
    debug(context);
    if (context.getExecution().getRequirement()
        == AuthenticationExecutionModel.Requirement.REQUIRED) {
      context.success();
    } else {
      context.attempted();
    }
  }

  private void debug(AuthenticationFlowContext context) {
    log.infof("requirement: %s", context.getExecution().getRequirement().toString());
    log.infof(
        "clientNotes: %s", writeValueAsJson(context.getAuthenticationSession().getClientNotes()));
    log.infof(
        "clientScopes: %s", writeValueAsJson(context.getAuthenticationSession().getClientScopes()));
    log.infof(
        "userSessionNotes: %s",
        writeValueAsJson(context.getAuthenticationSession().getUserSessionNotes()));
  }

  private static String writeValueAsJson(Object o) {
    try {
      return JsonSerialization.writeValueAsString(o);
    } catch (Exception e) {
      log.error("Error serializing", e);
    }
    return null;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return this;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false; // must return true for the Authenticator to call setRequiredActions()
  }

  @Override
  public String getHelpText() {
    return "Debugging use only.";
  }

  @Override
  public String getDisplayType() {
    return "Debugger";
  }

  @Override
  public String getReferenceCategory() {
    return "Debug";
  }
}
