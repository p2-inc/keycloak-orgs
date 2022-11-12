package io.phasetwo.service.auth.idp;

import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class IdpSelectorAuthenticatorFactory
    implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-auth-idp-selector";

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return new IdpSelectorAuthenticator(session);
  }

  private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
    AuthenticationExecutionModel.Requirement.REQUIRED,
    AuthenticationExecutionModel.Requirement.ALTERNATIVE,
    AuthenticationExecutionModel.Requirement.DISABLED
  };

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false; // must return true for the Authenticator to call setRequiredActions()
  }

  @Override
  public boolean isConfigurable() {
    return false; // only if we add a config property
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  private static final List<ProviderConfigProperty> configProperties =
      new ArrayList<ProviderConfigProperty>();

  @Override
  public String getHelpText() {
    return "Allows a user to select an IdP by alias and be redirected.";
  }

  @Override
  public String getDisplayType() {
    return "IdP Selector";
  }

  @Override
  public String getReferenceCategory() {
    return null;
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
