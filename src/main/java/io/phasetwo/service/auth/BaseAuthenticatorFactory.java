package io.phasetwo.service.auth;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public abstract class BaseAuthenticatorFactory
    implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

  private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
    AuthenticationExecutionModel.Requirement.REQUIRED,
    AuthenticationExecutionModel.Requirement.ALTERNATIVE,
    AuthenticationExecutionModel.Requirement.DISABLED
  };

  private final String providerId;
  private final AuthenticatorConfigProperties propsProvider;

  BaseAuthenticatorFactory(String providerId) {
    this(providerId, new AuthenticatorConfigProperties() {});
  }

  BaseAuthenticatorFactory(String providerId, AuthenticatorConfigProperties propsProvider) {
    this.providerId = providerId;
    this.propsProvider = propsProvider;
  }

  public AuthenticatorConfigProperties getPropsProvider() {
    return this.propsProvider;
  }

  @Override
  public abstract Authenticator create(KeycloakSession session);

  @Override
  public abstract String getDisplayType();

  @Override
  public abstract String getHelpText();

  @Override
  public String getId() {
    return this.providerId;
  }

  @Override
  public String getReferenceCategory() {
    return "alternate-auth";
  }

  @Override
  public boolean isConfigurable() {
    return propsProvider.getConfigProperties().size() > 0;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return true;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return propsProvider.getConfigProperties();
  }

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
