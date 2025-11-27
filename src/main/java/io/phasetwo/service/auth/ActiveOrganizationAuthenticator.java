package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;
import static org.keycloak.authentication.AuthenticationProcessor.CURRENT_FLOW_PATH;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.broker.util.PostBrokerLoginConstants;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;

@JBossLog
public class ActiveOrganizationAuthenticator implements Authenticator {
  public static final String POST_BROKER_FLOW = "post-broker-login";
  private final OrganizationProvider provider;
  private static final String BROWSER_ACCOUNT_HINT_PARAM = "client_request_param_account_hint";
  private static final String DIRECT_ACCOUNT_HINT = "account_hint";
  private static final String ERROR_FORM = "error.ftl";

  public ActiveOrganizationAuthenticator(KeycloakSession session) {
    this.provider = session.getProvider(OrganizationProvider.class);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    if (wasPostBrokerLogin(context)) {
      tryOrganizationSelectionChallenge(context);
    } else if (requestHasAccountHintParam(context)) {
      evaluateAuthenticationWithAccountHint(context);
    } else if (shouldChallengeForOrganizationSelection(context)) {
      tryOrganizationSelectionChallenge(context);
    } else {
      context.success();
    }
  }

  private boolean requestHasAccountHintParam(AuthenticationFlowContext context) {
    String browserAccountHintValue = getAccountHintValueFromBrowserRequest(context);
    String directGrantAccountHintValue = getAccountHintValueFromDirectGrantRequest(context);
    return !(browserAccountHintValue == null && directGrantAccountHintValue == null);
  }

  private String getAccountHintValueFromBrowserRequest(AuthenticationFlowContext context) {
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    return authSession.getClientNote(BROWSER_ACCOUNT_HINT_PARAM);
  }

  private String getAccountHintValueFromDirectGrantRequest(AuthenticationFlowContext context) {
    HttpRequest httpRequest = context.getHttpRequest();
    UriInfo uriInfo = httpRequest.getUri();
    MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
    return queryParams.getFirst(DIRECT_ACCOUNT_HINT);
  }

  private void evaluateAuthenticationWithAccountHint(AuthenticationFlowContext context) {
    String organizationId = getOrganizationIdFromAccountHint(context);
    evaluateAuthenticationChallenge(context, organizationId);
  }

  private String getOrganizationIdFromAccountHint(AuthenticationFlowContext context) {
    String accountHint = getAccountHintValueFromBrowserRequest(context);
    if (accountHint != null) {
      return accountHint;
    } else {
      return getAccountHintValueFromDirectGrantRequest(context);
    }
  }

  private void evaluateAuthenticationChallenge(
      AuthenticationFlowContext context, String organizationId) {
    if (hasMembership(context, organizationId)) {
      updateActiveOrganizationAttributeAndSucceedChallenge(context, organizationId);
    } else {
      failChallenge(context, "invalidOrganizationError");
    }
  }

  private boolean hasMembership(AuthenticationFlowContext context, String organizationId) {
    if (provider
        .getUserOrganizationsStream(context.getRealm(), context.getUser())
        .noneMatch(org -> org.getId().equals(organizationId))) {
      log.errorf("User isn't a member of this organization");
      return false;
    }
    return true;
  }

  private void updateActiveOrganizationAttributeAndSucceedChallenge(
      AuthenticationFlowContext context, String organizationIdFromHint) {
    log.debugf("Authentication Challenge Success");
    context
        .getUser()
        .setAttribute(ACTIVE_ORGANIZATION, Collections.singletonList(organizationIdFromHint));
    context.success();
  }

  private void failChallenge(AuthenticationFlowContext context, String errorMessage) {
    log.debugf("Authentication Challenge Failure");
    Response errorResponse;
    try {
      errorResponse = context.form().setError(errorMessage).createForm(ERROR_FORM);
    } catch (Exception e) {
      errorResponse = Response.status(401).build();
    }
    context.failureChallenge(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, errorResponse);
  }

  private boolean shouldChallengeForOrganizationSelection(AuthenticationFlowContext context) {

    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    String prompt = authSession.getClientNote(OIDCLoginProtocol.PROMPT_PARAM);
    return prompt != null && prompt.contains(OIDCLoginProtocol.PROMPT_VALUE_SELECT_ACCOUNT);
  }

  private boolean wasPostBrokerLogin(AuthenticationFlowContext context){
      String currentFlow = context.getAuthenticationSession().getAuthNote(CURRENT_FLOW_PATH);
      return POST_BROKER_FLOW.equals(currentFlow);
  }

  private void tryOrganizationSelectionChallenge(AuthenticationFlowContext context) {
    List<OrganizationModel> organizations =
        provider.getUserOrganizationsStream(context.getRealm(), context.getUser()).toList();

    if (organizations.isEmpty()) {
      log.warnf(
          "Select organization challenge couldn't be performed because the user has no organization.");
      failChallenge(context, "noOrganizationError");
    } else if (organizations.size() == 1) {
      log.debugf("User has 1 organization, skip organization selection challenge.");
      updateActiveOrganizationAttributeAndSucceedChallenge(context, organizations.get(0).getId());
    } else {
      LoginFormsProvider loginForm = context.form();
      loginForm.setAttribute("organizations", organizations);
      context.challenge(loginForm.createForm("ext-select-organization.ftl"));
    }
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    HttpRequest request = context.getHttpRequest();
    MultivaluedMap<String, String> formData = request.getDecodedFormParameters();
    String organizationId = formData.getFirst("organizationId");

    if (organizationId == null || organizationId.isEmpty()) {
      log.errorf("No selected organization");
      failChallenge(context, "invalidOrganizationError");
    } else {
      evaluateAuthenticationChallenge(context, organizationId);
    }
  }

  @Override
  public boolean requiresUser() {
    return true; // we need the user to look up the organizations
  }

  @Override
  public boolean configuredFor(
      KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
    return true;
  }

  @Override
  public void setRequiredActions(
      KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {}

  @Override
  public void close() {}
}
