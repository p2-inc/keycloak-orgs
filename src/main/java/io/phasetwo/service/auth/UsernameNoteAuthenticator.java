package io.phasetwo.service.auth;

import static org.keycloak.services.validation.Validation.FIELD_USERNAME;

import io.phasetwo.service.util.Emails;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.managers.AuthenticationManager;

@JBossLog
class UsernameNoteAuthenticator extends AbstractUsernameFormAuthenticator
    implements DefaultAuthenticator {

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
    String loginHint =
        context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

    String rememberMeUsername =
        AuthenticationManager.getRememberMeUsername(
            context.getRealm(), context.getHttpRequest().getHttpHeaders());

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

    log.infof("username set in auth note %s.", username);
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

    if (!Emails.isValidEmail(username)) {
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
}
