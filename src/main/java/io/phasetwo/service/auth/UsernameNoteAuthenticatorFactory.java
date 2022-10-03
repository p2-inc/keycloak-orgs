package io.phasetwo.service.auth;

import com.google.auto.service.AutoService;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

@AutoService(AuthenticatorFactory.class)
public class UsernameNoteAuthenticatorFactory extends BaseAuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-auth-username-auth-note";

  public UsernameNoteAuthenticatorFactory() {
    super(PROVIDER_ID);
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
