package io.phasetwo.service.auth;

import com.google.auto.service.AutoService;
import io.phasetwo.service.util.Emails;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.utils.StringUtil;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class ExistingSessionVerifierAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-existing-session-verifier";

  public ExistingSessionVerifierAuthenticatorFactory() {
    super(
        PROVIDER_ID,
        new AuthenticatorConfigProperties() {
          @Override
          public List<ProviderConfigProperty> getConfigProperties() {
            return ProviderConfigurationBuilder.create().build();
          }
        });
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.debug("ExistingSessionVerifierAuthenticatorFactory.action");
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.debug("ExistingSessionVerifierAuthenticatorFactory.authenticate");

    BrokeredIdentityContext brokerContext = PostOrgAuthFlow.getBrokeredIdentityContext(context);
    if (!PostOrgAuthFlow.brokeredIdpEnabled(context, brokerContext)) return;

    AuthenticationManager.AuthResult authResult =
        AuthenticationManager.authenticateIdentityCookie(
            context.getSession(), context.getRealm(), true);
    //Do the validation only if there is an active session in the browser
    if (authResult != null) {
      var email = brokerContext.getEmail();
      if (StringUtil.isBlank(email) || !Emails.isValidEmail(email)) {
          log.warnf(
                  "Brokered user %s does not have a valid email: %s.", context.getUser(), context.getUser().getEmail());

          invalidEmailFailChallenge(context, authResult.user().getUsername());
          return;
      }

      var authenticatedUserEmail = authResult.user().getEmail();
      if (StringUtil.isBlank(authenticatedUserEmail) || !Emails.isValidEmail(authenticatedUserEmail)) {
          log.warnf("Authenticated user email %s  not have a valid email: %s", authResult.user().getEmail(), authResult.user().getEmail());

          invalidEmailFailChallenge(context, authResult.user().getUsername() );
          return;
      }

      if (!authenticatedUserEmail.equals(email)) {
        log.debugf(
            "Authenticated user email %s does not matched brokered email %s.",
            authenticatedUserEmail, email);
          validationFailChallenge(context, authenticatedUserEmail);
        return;
      }
    }

    context.success();
  }

  @Override
  public boolean requiresUser() {
    return true;
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
    return "Check if there is a existing authentication session. If so check if the existing session email is the same as the one obtained from the Brokered Identity context. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Existing Session Email verifier";
  }

  @Override
  public String getReferenceCategory() {
    return "Post Broker";
  }

  private void validationFailChallenge(AuthenticationFlowContext context,
                                         String email) {
    log.debugf("Authentication Challenge Failure validation error");
    Response challengeResponse =
        context.form().setError("existingSessionValidationError", email)
                .createErrorPage(Response.Status.BAD_REQUEST);
    context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challengeResponse);
  }

    private void invalidEmailFailChallenge(AuthenticationFlowContext context,
                                            String username) {
        log.debugf("Authentication Challenge Failure Invalid email");
        Response challengeResponse =
                context.form().setError("existingSessionInvalidEmail", username)
                        .createErrorPage(Response.Status.BAD_REQUEST);
        context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challengeResponse);
    }
}
