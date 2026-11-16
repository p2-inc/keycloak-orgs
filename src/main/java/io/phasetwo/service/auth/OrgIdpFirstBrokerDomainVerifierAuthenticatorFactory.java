package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.KC_ORGS_SKIP_MIGRATION;
import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static io.phasetwo.service.Orgs.REQUIRE_VERIFIED_DOMAIN;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

import com.google.auto.service.AutoService;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * First-broker-login authenticator that verifies the <em>brokered</em> email domain belongs to one
 * of the organizations that own the IdP, <strong>before</strong> {@code idp-create-user-if-unique}
 * creates (or links) the account.
 *
 * <p>This closes the account-takeover path (GHSA-gvcc-4vw2-6wvr) that the post-broker {@link
 * OrgIdpDomainVerifierAuthenticatorFactory} cannot: that one requires an existing user
 * ({@code requiresUser() == true}) and therefore only runs <em>after</em> first broker login has
 * already created the user and stored the federated-identity link, so a rejected login still leaves
 * a persistent account/link for an out-of-domain email. By checking the brokered email domain here,
 * an org IdP can no longer create accounts for email addresses outside the org's domains, and
 * editing the email on the Review Profile page cannot smuggle an out-of-domain address past the
 * check (this execution is placed immediately after {@code idp-review-profile}).
 *
 * <p>The authenticator is user-less ({@code requiresUser() == false}) and is a no-op for non-org
 * IdPs, so it is safe to sit in the realm-wide {@code first broker login} flow.
 */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class OrgIdpFirstBrokerDomainVerifierAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-org-domain-fbl-verifier";

  private static final ProviderConfigProperty REQUIRE_VERIFIED_DOMAIN_PROPERTY =
      new ProviderConfigProperty(
          REQUIRE_VERIFIED_DOMAIN,
          "Require a verified domain",
          "Whether a verified domain name for an organization is required for the authentication.",
          BOOLEAN_TYPE,
          false,
          false);

  public OrgIdpFirstBrokerDomainVerifierAuthenticatorFactory() {
    super(
        PROVIDER_ID,
        new AuthenticatorConfigProperties() {
          @Override
          public List<ProviderConfigProperty> getConfigProperties() {
            return ProviderConfigurationBuilder.create()
                .property(REQUIRE_VERIFIED_DOMAIN_PROPERTY)
                .build();
          }
        });
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.debug("OrgIdpFirstBrokerDomainVerifierAuthenticatorFactory.action");
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.debug("OrgIdpFirstBrokerDomainVerifierAuthenticatorFactory.authenticate");

    AuthenticationSessionModel authSession = context.getAuthenticationSession();
    SerializedBrokeredIdentityContext serializedCtx =
        SerializedBrokeredIdentityContext.readFromAuthenticationSession(
            authSession, AbstractIdpAuthenticator.BROKERED_CONTEXT_NOTE);
    if (serializedCtx == null) {
      // Not a (first) broker login; nothing to verify. Pass this required gate.
      context.success();
      return;
    }

    BrokeredIdentityContext brokerContext =
        serializedCtx.deserialize(context.getSession(), authSession);
    Map<String, String> idpConfig = brokerContext.getIdpConfig().getConfig();
    if (idpConfig == null || !idpConfig.containsKey(ORG_OWNER_CONFIG_KEY)) {
      // Not an organization IdP; do not interfere with ordinary broker logins.
      context.success();
      return;
    }

    boolean requireVerifiedDomain = requireVerifiedDomain(context.getAuthenticatorConfig());

    if (PostOrgAuthFlow.emailDomainMatchesOrg(
        context.getSession(),
        context.getRealm(),
        idpConfig,
        brokerContext.getEmail(),
        requireVerifiedDomain)) {
      context.success();
    } else {
      log.warnf(
          "Blocking first broker login for IdP %s: email domain of '%s' is not owned by the"
              + " organization(s) that own this IdP.",
          brokerContext.getIdpConfig().getAlias(), brokerContext.getEmail());
      context
          .getEvent()
          .clone()
          .user(brokerContext.getEmail())
          .error("User email domain could not be found in the organizations that own this IdP.");
      failChallenge(context, "validDomainNotFound");
    }
  }

  @Override
  public boolean requiresUser() {
    return false;
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
    return "Check that the brokered email domain is owned by the organization before an organization"
        + " IdP can create or link a user. Use only in First Broker Login flows.";
  }

  @Override
  public String getDisplayType() {
    return "Org IDP first broker domain verifier";
  }

  @Override
  public String getReferenceCategory() {
    return "First Broker";
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    factory.register(
        (ProviderEvent ev) -> {
          if (ev instanceof RealmModel.RealmPostCreateEvent) {
            PostOrgAuthFlow.firstBrokerPostCreate(
                (RealmModel.RealmPostCreateEvent) ev,
                PROVIDER_ID,
                AuthenticationExecutionModel.Requirement.REQUIRED);
          } else if (ev instanceof PostMigrationEvent) {
            // Retrofit existing realms so the account-takeover fix applies on upgrade, not only to
            // realms created afterwards. Opt out with KC_ORGS_SKIP_MIGRATION.
            if (KC_ORGS_SKIP_MIGRATION == null) {
              KeycloakModelUtils.runJobInTransaction(
                  factory,
                  session ->
                      session
                          .realms()
                          .getRealmsStream()
                          .forEach(
                              realm ->
                                  PostOrgAuthFlow.addFirstBrokerExecution(
                                      session,
                                      realm,
                                      PROVIDER_ID,
                                      AuthenticationExecutionModel.Requirement.REQUIRED)));
            }
          }
        });
  }

  private void failChallenge(AuthenticationFlowContext context, String errorMessage) {
    log.debugf("Authentication Challenge Failure");
    Response challengeResponse =
        context.form().setError(errorMessage).createErrorPage(Response.Status.BAD_REQUEST);
    context.failureChallenge(AuthenticationFlowError.IDENTITY_PROVIDER_ERROR, challengeResponse);
  }

  private boolean requireVerifiedDomain(AuthenticatorConfigModel authenticatorConfigModel) {
    return Optional.ofNullable(authenticatorConfigModel)
        .map(
            it ->
                Boolean.parseBoolean(it.getConfig().getOrDefault(REQUIRE_VERIFIED_DOMAIN, "false")))
        .orElse(false);
  }
}
