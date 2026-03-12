package io.phasetwo.service.auth;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * Factory for {@link ConditionalOrgAttributeMatchesClientAttribute}.
 *
 * <p>Registers a conditional authenticator that compares a Phase Two organization attribute
 * against a value read from the current OIDC client's attributes at runtime.
 */
public class ConditionalOrgAttributeMatchesClientAttributeFactory
    implements ConditionalAuthenticatorFactory {

  /** SPI provider ID — must be unique across all deployed providers. MAX 36 chars (DB column limit). */
  public static final String PROVIDER_ID = "conditional-org-client-attr";

  // --- Config property keys ---

  /** The key of the organization attribute to check. Example: {@code "tenant_id"} */
  public static final String CONF_ORG_ATTRIBUTE_NAME = "org_attribute_name";

  /**
   * The key of the client attribute that holds the expected value. Example: {@code "tenant_id"}
   * (set on the client entity, not a protocol mapper)
   */
  public static final String CONF_CLIENT_ATTRIBUTE_NAME = "client_attribute_name";

  /** Whether to invert the match result. When {@code true}, the subflow fires when the attribute does NOT match. */
  public static final String CONF_NEGATE = "negate_output";

  // --- Config property definitions (rendered in the Admin Console) ---

  private static final List<ProviderConfigProperty> CONFIG_PROPERTIES =
      List.of(
          new ProviderConfigProperty(
              CONF_ORG_ATTRIBUTE_NAME,
              "Org attribute name",
              "The organization attribute key to check (e.g. tenant_id).",
              ProviderConfigProperty.STRING_TYPE,
              ""),
          new ProviderConfigProperty(
              CONF_CLIENT_ATTRIBUTE_NAME,
              "Client attribute name",
              "The client attribute key that holds the expected value (e.g. tenant_id). Read from the current OIDC client at runtime.",
              ProviderConfigProperty.STRING_TYPE,
              ""),
          new ProviderConfigProperty(
              CONF_NEGATE,
              "Negate output",
              "Invert the condition result. When ON, the subflow executes only when the attribute does NOT match.",
              ProviderConfigProperty.BOOLEAN_TYPE,
              "false"));

  @Override
  public ConditionalAuthenticator getSingleton() {
    return ConditionalOrgAttributeMatchesClientAttribute.SINGLETON;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayType() {
    return "Condition - org attribute matches client attribute";
  }

  @Override
  public String getReferenceCategory() {
    return "condition";
  }

  @Override
  public String getHelpText() {
    return "Matches when the authenticating user belongs to a Phase Two organization whose attribute (e.g. tenant_id) equals the value of the specified attribute on the current OIDC client. Designed for per-client tenant enforcement in multi-tenant IdP setups.";
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return new AuthenticationExecutionModel.Requirement[] {
      AuthenticationExecutionModel.Requirement.REQUIRED,
      AuthenticationExecutionModel.Requirement.DISABLED
    };
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return CONFIG_PROPERTIES;
  }

  @Override
  public void init(Config.Scope scope) {
    // no-op
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    // no-op
  }

  @Override
  public void close() {
    // no-op
  }
}
