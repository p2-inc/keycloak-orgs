package io.phasetwo.service.auth;

import com.google.auto.service.AutoService;
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
public class BrokerDomainVerifierAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-domain-verifier";

  public BrokerDomainVerifierAuthenticatorFactory() {
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
    log.debug("BrokerDomainVerifierAuthenticatorFactory.action");
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.debug("BrokerDomainVerifierAuthenticatorFactory.authenticate");

    BrokeredIdentityContext brokerContext = PostOrgAuthFlow.getBrokeredIdentityContext(context);
    if (!PostOrgAuthFlow.brokeredIdpEnabled(context, brokerContext)) return;

    var email = brokerContext.getEmail();

    if (StringUtil.isBlank(email)) {
        log.warnf(
                "Brokered user %s does not contain a email {}. Validation not performed", context.getUser(), context.getUser().getEmail());
    }

    AuthenticationManager.AuthResult authResult =
        AuthenticationManager.authenticateIdentityCookie(
            context.getSession(), context.getRealm(), true);
    if (authResult != null) {
      var authenticatedUserEmail = authResult.user().getEmail();
      if (StringUtil.isBlank(authenticatedUserEmail)) {
        log.warnf(
            "Authenticated user email %s does not have a email {}. Validation not performed",
            authResult.user().getEmail(), authResult.user().getEmail());
      }

      if (StringUtil.isNotBlank(authenticatedUserEmail)
           && StringUtil.isNotBlank(email)
           && !authenticatedUserEmail.equals(email)) {
        log.debugf(
            "Authenticated user email %s does not matched brokered email {}.",
            authenticatedUserEmail, email);
        failChallenge(context, authenticatedUserEmail);
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
    return "Check if the email of the user that logins via IdP is the same one which is obtained in the response. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Brokered Email verifier";
  }

  @Override
  public String getReferenceCategory() {
    return "Post Broker";
  }

  private void failChallenge(AuthenticationFlowContext context, String email) {
    log.debugf("Authentication Challenge Failure");
    Response challengeResponse =
        context.form().setError("brokeredEmailValidationError", email)
                .createErrorPage(Response.Status.BAD_REQUEST);
    context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challengeResponse);
  }
}
