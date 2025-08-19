package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.REQUIRE_VALID_EMAIL;
import static io.phasetwo.service.Orgs.SET_USER_IN_CONTEXT;
import static org.keycloak.protocol.oidc.OIDCLoginProtocol.LOGIN_HINT_PARAM;
import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

import io.phasetwo.service.util.Emails;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

@JBossLog
class UsernameNoteAuthenticator extends AbstractUsernameFormAuthenticator
    implements DefaultAuthenticator {

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    String loginHint =
        context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

    String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getSession());

    if (loginHint != null || rememberMeUsername != null) {
      if (loginHint != null) {
        formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
      } else {
        formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
        formData.add("rememberMe", "on");
      }
    }
    Response challengeResponse = challenge(context, formData);
    context.challenge(challengeResponse);
  }

  protected Response challenge(
      AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
    LoginFormsProvider forms = context.form();
    if (!formData.isEmpty()) {
      forms.setFormData(formData);
    }
    return forms.createLoginUsername();
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    if (formData.containsKey("cancel")) {
      context.cancelLogin();
      return;
    }

    String username = setUserInContext(context, formData);
    if (username == null) {
      return;
    }

    log.debugf("username set in auth note %s.", username);
    if (context.getExecution().getRequirement()
        == AuthenticationExecutionModel.Requirement.REQUIRED) {
      context.success();
    } else {
      context.attempted();
    }
  }

  private String setUserInContext(
      AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
    context.clearUser();

    String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);

    if (username != null) {
      username = username.trim();
      if ("".equalsIgnoreCase(username)) username = null;
    }

    if (username == null) {
      context.getEvent().error(Errors.USER_NOT_FOUND);
      Response challengeResponse =
          challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
      context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
      return null;
    }

    if (isRequireValidEmail(context.getAuthenticatorConfig()) && !Emails.isValidEmail(username)) {
      context.getEvent().error(Errors.INVALID_EMAIL);
      Response challengeResponse =
          challenge(context, getDefaultChallengeMessage(context), FIELD_USERNAME);
      context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
      return null;
    }

    context.getEvent().detail(Details.USERNAME, username);
    context
        .getAuthenticationSession()
        .setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
    context.getAuthenticationSession().setClientNote(LOGIN_HINT_PARAM, username);

    if (isSetUserInContext(context.getAuthenticatorConfig())) {
      try {
        UserModel user =
            KeycloakModelUtils.findUserByNameOrEmail(
                context.getSession(), context.getRealm(), username);
        if (user != null) {
          log.debugf("Setting user '%s' in context", user.getId());
          context.setUser(user);
        }
      } catch (ModelDuplicateException e) {
        log.warnf(
            e,
            "Could not uniquely identify the user. Multiple users with name or email '%s' found.",
            username);
      }
    }

    return username;
  }

  @Override
  protected Response createLoginForm(LoginFormsProvider form) {
    return form.createLoginUsername();
  }

  @Override
  protected String getDefaultChallengeMessage(AuthenticationFlowContext context) {
    return context.getRealm().isLoginWithEmailAllowed()
        ? "invalidUsernameOrEmailMessage"
        : "invalidUsernameMessage";
  }

  private boolean isSetUserInContext(AuthenticatorConfigModel authenticatorConfigModel) {
    return Optional.ofNullable(authenticatorConfigModel)
        .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(SET_USER_IN_CONTEXT, "false")))
        .orElse(false);
  }

  private boolean isRequireValidEmail(AuthenticatorConfigModel authenticatorConfigModel) {
    return Optional.ofNullable(authenticatorConfigModel)
        .map(it -> Boolean.parseBoolean(it.getConfig().getOrDefault(REQUIRE_VALID_EMAIL, "true")))
        .orElse(false);
  }
}
