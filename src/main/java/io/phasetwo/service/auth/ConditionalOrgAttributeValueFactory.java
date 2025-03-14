package io.phasetwo.service.auth;

import com.google.auto.service.AutoService;
import java.util.Arrays;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

@JBossLog
@AutoService(AuthenticatorFactory.class)
public class ConditionalOrgAttributeValueFactory implements ConditionalAuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-auth-conditional-org-attribute";

  public static final String CONF_ATTRIBUTE_NAME = "org_attribute_name";
  public static final String CONF_ATTRIBUTE_EXPECTED_VALUE = "org_attribute_expected_value";
  public static final String CONF_ALL_ORGS = "org_attribute_all_orgs";
  public static final String CONF_NOT = "org_attribute_not";
  public static final String REGEX = "regex";

  private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
    AuthenticationExecutionModel.Requirement.REQUIRED,
    AuthenticationExecutionModel.Requirement.DISABLED
  };

  @Override
  public void init(Config.Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayType() {
    return "Condition - organization attribute";
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Flow is executed only if the org attribute exists and has the expected value";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    ProviderConfigProperty attributeName = new ProviderConfigProperty();
    attributeName.setType(ProviderConfigProperty.STRING_TYPE);
    attributeName.setName(CONF_ATTRIBUTE_NAME);
    attributeName.setLabel("Attribute name");
    attributeName.setHelpText("Name of the org attribute to check");

    ProviderConfigProperty attributeExpectedValue = new ProviderConfigProperty();
    attributeExpectedValue.setType(ProviderConfigProperty.STRING_TYPE);
    attributeExpectedValue.setName(CONF_ATTRIBUTE_EXPECTED_VALUE);
    attributeExpectedValue.setLabel("Expected attribute value");
    attributeExpectedValue.setHelpText("Expected value in the attribute");

    ProviderConfigProperty allOrgs = new ProviderConfigProperty();
    allOrgs.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    allOrgs.setName(CONF_ALL_ORGS);
    allOrgs.setLabel("All Organizations");
    allOrgs.setHelpText("Check all orgs, or just the active org");

    ProviderConfigProperty negateOutput = new ProviderConfigProperty();
    negateOutput.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    negateOutput.setName(CONF_NOT);
    negateOutput.setLabel("Negate output");
    negateOutput.setHelpText("Apply a not to the check result");

    return Arrays.asList(attributeName, attributeExpectedValue, allOrgs, negateOutput);
  }

  @Override
  public ConditionalAuthenticator getSingleton() {
    return ConditionalOrgAttributeValue.SINGLETON;
  }
}
