package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.KC_ORGS_SKIP_MIGRATION;
import static io.phasetwo.service.Orgs.ORG_BROWSER_AUTH_FLOW_ALIAS;
import static io.phasetwo.service.Orgs.ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS;

import com.google.auto.service.AutoService;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.authentication.AuthenticationFlow;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.CookieAuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.IdentityProviderAuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.OTPFormAuthenticatorFactory;
import org.keycloak.authentication.authenticators.browser.UsernamePasswordFormFactory;
import org.keycloak.authentication.authenticators.conditional.ConditionalUserConfiguredAuthenticatorFactory;
import org.keycloak.authentication.authenticators.directgrant.ValidatePassword;
import org.keycloak.authentication.authenticators.directgrant.ValidateUsername;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmModel.RealmPostCreateEvent;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderEvent;

@JBossLog
@AutoService(AuthenticatorFactory.class)
public class ActiveOrganizationAuthenticatorFactory implements AuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-select-org";
  public static final String PROVIDER_DISPLAY = "Select Organization";
  public static final String PROVIDER_HELP_TEXT = "Select the current Organization on Login";

  private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
    AuthenticationExecutionModel.Requirement.REQUIRED,
    AuthenticationExecutionModel.Requirement.DISABLED
  };

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayType() {
    return PROVIDER_DISPLAY;
  }

  @Override
  public String getReferenceCategory() {
    return "organization";
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  @Override
  public Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return PROVIDER_HELP_TEXT;
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return null;
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return new ActiveOrganizationAuthenticator(session);
  }

  @Override
  public void init(Scope scope) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    factory.register(
        (ProviderEvent event) -> {
          if (event instanceof RealmModel.RealmPostCreateEvent) {
            createOrgBrowserFlow(((RealmPostCreateEvent) event).getCreatedRealm());
            createOrgDirectGrantFlow(((RealmPostCreateEvent) event).getCreatedRealm());
          } else if (event instanceof PostMigrationEvent) {
            log.debug("PostMigrationEvent");
            if (KC_ORGS_SKIP_MIGRATION == null) {
              log.debug(
                  "initializing active organization user profile attribute following migration");
              KeycloakModelUtils.runJobInTransaction(factory, this::postMigrationCreateAuthFlow);
            }
          }
        });
  }

  @Override
  public void close() {}

  private void postMigrationCreateAuthFlow(KeycloakSession session) {
    log.debug("ActiveOrganizationAuthenticatorFactory::postMigrationCreateAuthFlow");
    session
        .realms()
        .getRealmsStream()
        .forEach(
            realm -> {
              createOrgBrowserFlow(realm);
              createOrgDirectGrantFlow(realm);
            });
  }

  private void createOrgBrowserFlow(RealmModel realm) {
    AuthenticationFlowModel browser = realm.getFlowByAlias(ORG_BROWSER_AUTH_FLOW_ALIAS);
    if (browser != null) {
      log.debugf("%s flow exists. Skipping.", ORG_BROWSER_AUTH_FLOW_ALIAS);
      return;
    }

    log.infof("creating built-in auth flow for %s", ORG_BROWSER_AUTH_FLOW_ALIAS);
    browser = new AuthenticationFlowModel();
    browser.setAlias(ORG_BROWSER_AUTH_FLOW_ALIAS);
    browser.setDescription("Browser flow with select organization step.");
    browser.setProviderId(AuthenticationFlow.BASIC_FLOW);
    browser.setTopLevel(true);
    browser.setBuiltIn(true);
    browser = realm.addAuthenticationFlow(browser);

    // Cookie sub-flow
    cookieSubFlow(browser.getId(), realm);

    // IDP sub-flow
    identityProviderSubFlow(browser.getId(), realm);

    // Username Password sub-flow
    usernamePasswordSubFlow(browser.getId(), realm);
  }

  private void createOrgDirectGrantFlow(RealmModel realm) {
    AuthenticationFlowModel grant = realm.getFlowByAlias(ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS);
    if (grant != null) {
      log.debugf("%s flow exists. Skipping.", ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS);
      return;
    }

    log.infof("creating built-in auth flow for %s", ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS);
    grant = new AuthenticationFlowModel();
    grant.setAlias(ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS);
    grant.setDescription("Direct grant flow with select organization step.");
    grant.setProviderId(AuthenticationFlow.BASIC_FLOW);
    grant.setTopLevel(true);
    grant.setBuiltIn(true);
    grant = realm.addAuthenticationFlow(grant);

    // username
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(grant.getId());
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(ValidateUsername.PROVIDER_ID);
    execution.setPriority(10);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);

    // password
    execution = new AuthenticationExecutionModel();
    execution.setParentFlow(grant.getId());
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(ValidatePassword.PROVIDER_ID);
    execution.setPriority(20);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);

    // otp
    conditionalOtp(grant.getId(), realm, "Org Direct Grant - Conditional OTP", 30);

    // select org
    selectOrgExecution(grant.getId(), realm, 30);
  }

  private void cookieSubFlow(String parentFlowId, RealmModel realm) {
    AuthenticationFlowModel cookiesSubFlow = new AuthenticationFlowModel();
    cookiesSubFlow.setTopLevel(false);
    cookiesSubFlow.setBuiltIn(true);
    cookiesSubFlow.setAlias("Cookies Sub-Flow");
    cookiesSubFlow.setDescription("Cookie sub-flow which can be used to switch org.");
    cookiesSubFlow.setProviderId(AuthenticationFlow.BASIC_FLOW);
    cookiesSubFlow = realm.addAuthenticationFlow(cookiesSubFlow);
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(parentFlowId);
    execution.setRequirement(AuthenticationExecutionModel.Requirement.ALTERNATIVE);
    execution.setFlowId(cookiesSubFlow.getId());
    execution.setPriority(10);
    execution.setAuthenticatorFlow(true);
    realm.addAuthenticatorExecution(execution);

    execution = new AuthenticationExecutionModel();
    execution.setParentFlow(cookiesSubFlow.getId());
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(CookieAuthenticatorFactory.PROVIDER_ID);
    execution.setPriority(10);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);

    selectOrgExecution(cookiesSubFlow.getId(), realm, 20);
  }

  private void identityProviderSubFlow(String parentFlowId, RealmModel realm) {
    AuthenticationFlowModel idpSubFLow = new AuthenticationFlowModel();
    idpSubFLow.setTopLevel(false);
    idpSubFLow.setBuiltIn(true);
    idpSubFLow.setAlias("IDP Sub-Flow");
    idpSubFLow.setDescription("IDP sub-flow to select org.");
    idpSubFLow.setProviderId(AuthenticationFlow.BASIC_FLOW);
    idpSubFLow = realm.addAuthenticationFlow(idpSubFLow);
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(parentFlowId);
    execution.setRequirement(AuthenticationExecutionModel.Requirement.ALTERNATIVE);
    execution.setFlowId(idpSubFLow.getId());
    execution.setPriority(20);
    execution.setAuthenticatorFlow(true);
    realm.addAuthenticatorExecution(execution);

    execution = new AuthenticationExecutionModel();
    execution.setParentFlow(idpSubFLow.getId());
    execution.setRequirement(Requirement.REQUIRED);
    execution.setAuthenticator(IdentityProviderAuthenticatorFactory.PROVIDER_ID);
    execution.setPriority(10);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);

    selectOrgExecution(idpSubFLow.getId(), realm, 20);
  }

  private void usernamePasswordSubFlow(String parentFlowId, RealmModel realm) {
    AuthenticationFlowModel usernamePasswordSubFlow = new AuthenticationFlowModel();
    usernamePasswordSubFlow.setTopLevel(false);
    usernamePasswordSubFlow.setBuiltIn(true);
    usernamePasswordSubFlow.setAlias("Forms Sub-Flow");
    usernamePasswordSubFlow.setDescription("Username, password, otp and other auth forms.");
    usernamePasswordSubFlow.setProviderId(AuthenticationFlow.BASIC_FLOW);
    usernamePasswordSubFlow = realm.addAuthenticationFlow(usernamePasswordSubFlow);
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(parentFlowId);
    execution.setRequirement(AuthenticationExecutionModel.Requirement.ALTERNATIVE);
    execution.setFlowId(usernamePasswordSubFlow.getId());
    execution.setPriority(30);
    execution.setAuthenticatorFlow(true);
    realm.addAuthenticatorExecution(execution);

    // Username Password processing
    execution = new AuthenticationExecutionModel();
    execution.setParentFlow(usernamePasswordSubFlow.getId());
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(UsernamePasswordFormFactory.PROVIDER_ID);
    execution.setPriority(10);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);

    // Conditional OTP
    conditionalOtp(usernamePasswordSubFlow.getId(), realm, "Org Browser - Conditional OTP", 20);
    // Select ORG
    selectOrgExecution(usernamePasswordSubFlow.getId(), realm, 30);
  }

  private void selectOrgExecution(String parentFlowId, RealmModel realm, int priority) {
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(parentFlowId);
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(PROVIDER_ID);
    execution.setPriority(priority);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);
  }

  private void conditionalOtp(String parentFlowId, RealmModel realm, String alias, int priority) {
    AuthenticationFlowModel conditionalOTP = new AuthenticationFlowModel();
    conditionalOTP.setTopLevel(false);
    conditionalOTP.setBuiltIn(true);
    conditionalOTP.setAlias(alias);
    conditionalOTP.setDescription(
        "Flow to determine if the OTP is required for the authentication");
    conditionalOTP.setProviderId(AuthenticationFlow.BASIC_FLOW);
    conditionalOTP = realm.addAuthenticationFlow(conditionalOTP);
    AuthenticationExecutionModel execution = new AuthenticationExecutionModel();
    execution.setParentFlow(parentFlowId);
    execution.setRequirement(AuthenticationExecutionModel.Requirement.CONDITIONAL);
    execution.setFlowId(conditionalOTP.getId());
    execution.setPriority(priority);
    execution.setAuthenticatorFlow(true);
    realm.addAuthenticatorExecution(execution);

    execution = new AuthenticationExecutionModel();
    execution.setParentFlow(conditionalOTP.getId());
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(ConditionalUserConfiguredAuthenticatorFactory.PROVIDER_ID);
    execution.setPriority(10);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);

    execution = new AuthenticationExecutionModel();
    execution.setParentFlow(conditionalOTP.getId());
    execution.setRequirement(AuthenticationExecutionModel.Requirement.REQUIRED);
    execution.setAuthenticator(OTPFormAuthenticatorFactory.PROVIDER_ID);
    execution.setPriority(20);
    execution.setAuthenticatorFlow(false);
    realm.addAuthenticatorExecution(execution);
  }
}
