package io.phasetwo.service.auth.invitation;

import com.google.auto.service.AutoService;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

@JBossLog
@AutoService(AuthenticatorFactory.class)
public class InvitationAutoJoinAuthenticatorFactory implements AuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-inv-auto-join";

  private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
    AuthenticationExecutionModel.Requirement.REQUIRED,
    AuthenticationExecutionModel.Requirement.DISABLED
  };

  @Override
  public Authenticator create(KeycloakSession session) {
    return new InvitationAutoJoinAuthenticator();
  }

  @Override
  public void init(Config.Scope scope) {}

  @Override
  public void postInit(KeycloakSessionFactory keycloakSessionFactory) {}

  @Override
  public void close() {}

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Adds a user to an organization if an invitation was sent to the user's corresponding email.";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return List.of();
  }

  @Override
  public String getDisplayType() {
    return "Auto Join Invitations";
  }

  @Override
  public String getReferenceCategory() {
    return "Authorization";
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }
}
