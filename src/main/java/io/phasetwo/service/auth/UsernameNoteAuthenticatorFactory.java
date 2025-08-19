package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.REQUIRE_VALID_EMAIL;
import static io.phasetwo.service.Orgs.SET_USER_IN_CONTEXT;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

import com.google.auto.service.AutoService;
import java.util.List;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

@AutoService(AuthenticatorFactory.class)
public class UsernameNoteAuthenticatorFactory extends BaseAuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-auth-username-auth-note";

  private static final ProviderConfigProperty SET_USER_IN_CONTEXT_PROPERTY =
      new ProviderConfigProperty(
          SET_USER_IN_CONTEXT,
          "Set user in context",
          "If the user exists, set them in the context.",
          BOOLEAN_TYPE,
          false,
          false);

  private static final ProviderConfigProperty REQUIRE_VALID_EMAIL_PROPERTY =
      new ProviderConfigProperty(
          REQUIRE_VALID_EMAIL,
          "Require valid email",
          "Require the provided username to be a valid email address.",
          BOOLEAN_TYPE,
          true,
          false);

  public UsernameNoteAuthenticatorFactory() {
    super(
        PROVIDER_ID,
        new AuthenticatorConfigProperties() {
          @Override
          public List<ProviderConfigProperty> getConfigProperties() {
            return ProviderConfigurationBuilder.create()
                .property(SET_USER_IN_CONTEXT_PROPERTY)
                .property(REQUIRE_VALID_EMAIL_PROPERTY)
                .build();
          }
        });
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return new UsernameNoteAuthenticator();
  }

  @Override
  public String getDisplayType() {
    return "Username Auth Note Form";
  }

  @Override
  public String getHelpText() {
    return "Accepts a username and sets it as an auth note";
  }
}
