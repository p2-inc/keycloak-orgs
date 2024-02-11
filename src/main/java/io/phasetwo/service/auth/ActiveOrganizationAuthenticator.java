package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;

@JBossLog
public class ActiveOrganizationAuthenticator implements Authenticator {
  private final OrganizationProvider provider;
  private static final String BROWSER_ACCOUNT_HINT_PARAM = "client_request_param_account_hint";
  private static final String DIRECT_ACCOUNT_HINT = "account_hint";
  private static final String ERROR_FORM = "error.ftl";

  public ActiveOrganizationAuthenticator(KeycloakSession session) {
    this.provider =  session.getProvider(OrganizationProvider.class);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    String prompt = authSession.getClientNote(OIDCLoginProtocol.PROMPT_PARAM);
    String accountHint = getAccountHint(context);

    // prompt is null and account hint is null
    if(!hasSelectAccount(prompt) && accountHint == null) {
      context.success();
    }
    // prompt is not null but account hint is null
    else if(hasSelectAccount(prompt) && accountHint == null) {
      selectAccountForm(context);
    }
    else { // other case
      evaluateAuthenticationChallenge(context, accountHint);
    }
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
    String organizationId = formData.getFirst("organizationId");

    if (organizationId == null || organizationId.isEmpty()) {
      log.errorf("No selected organization");
      Response errorResponse = context.form().setError("invalidOrganizationError").createForm(ERROR_FORM);
      context.failureChallenge(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, errorResponse);
    } else {
      evaluateAuthenticationChallenge(context, organizationId);
    }
  }

  private boolean hasSelectAccount(String prompt) {
    return prompt != null && prompt.contains(OIDCLoginProtocol.PROMPT_VALUE_SELECT_ACCOUNT);
  }

  private String getAccountHint(AuthenticationFlowContext context) {
    String accountHint = context.getAuthenticationSession().getClientNote(BROWSER_ACCOUNT_HINT_PARAM);
    if (accountHint != null) {
      return accountHint;
    }

    // to enable it for direct grant flow
    return context.getHttpRequest().getUri().getQueryParameters().getFirst(DIRECT_ACCOUNT_HINT);
  }

  private void selectAccountForm(AuthenticationFlowContext context) {
    List<OrganizationModel> organizations = provider
        .getUserOrganizationsStream(context.getRealm(), context.getUser()).toList();

    if (organizations.isEmpty()) {
      log.errorf("Select organization failed as user don't have an organization");
      Response errorResponse = context.form().setError("noOrganizationError").createForm(ERROR_FORM);
      context.failureChallenge(AuthenticationFlowError.INVALID_USER, errorResponse);
      log.debugf("Authentication Challenge Failure");
    }
    else if (organizations.size() == 1) { // skip challenge if user have only 1 org
      log.infof("User has 1 organization, skip selection");
      context.getUser().setAttribute(ACTIVE_ORGANIZATION, Collections.singletonList(organizations.get(0).getId()));
      context.success();
      log.debugf("Authentication Challenge Success");
    }
    else {
      LoginFormsProvider form = context.form().setAttribute("organizations", organizations);
      context.challenge(form.createForm("select-organization.ftl"));
    }
  }

  private boolean hasMembership(AuthenticationFlowContext context, String organizationId) {
    if (provider.getUserOrganizationsStream(context.getRealm(), context.getUser())
        .noneMatch(org -> org.getId().equals(organizationId))) {
      log.errorf("User isn't a member of this organization");
      return false;
    }
    return true;
  }

  private void evaluateAuthenticationChallenge(
      AuthenticationFlowContext context, String organizationId) {
    if(!hasMembership(context, organizationId)) {
      log.errorf("User isn't a member of this organization");
      Response errorResponse;
      try {
        errorResponse = context.form().setError("invalidOrganizationError").createForm(ERROR_FORM);
      } catch (Exception e) {
        errorResponse = Response.status(401).build();
      }
      context.failureChallenge(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, errorResponse);
      log.debugf("Authentication Challenge Failure");
    }
    else {
      context.getUser().setAttribute(ACTIVE_ORGANIZATION, Collections.singletonList(organizationId));
      context.success();
      log.debugf("Authentication Challenge Success");
    }
  }

  @Override
  public boolean requiresUser() {
    return true; // we need the user to look up the organizations
  }

  @Override
  public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel,
      UserModel userModel) {
    return true;
  }

  @Override
  public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel,
      UserModel userModel) {}

  @Override
  public void close() {}
}
