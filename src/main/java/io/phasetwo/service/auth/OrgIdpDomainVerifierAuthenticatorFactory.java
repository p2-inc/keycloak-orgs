package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static io.phasetwo.service.Orgs.REQUIRE_VERIFIED_DOMAIN;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;

import com.google.auto.service.AutoService;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.util.Domains;
import io.phasetwo.service.util.IdentityProviders;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.provider.ProviderEvent;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class OrgIdpDomainVerifierAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-org-id-verifier";

  private static final ProviderConfigProperty REQUIRE_VERIFIED_DOMAIN_PROPERTY =
      new ProviderConfigProperty(
          REQUIRE_VERIFIED_DOMAIN,
          "Require a verified domain",
          "Whether a verified domain name for an organization is required for the authentication.",
          BOOLEAN_TYPE,
          false,
          false);

  public OrgIdpDomainVerifierAuthenticatorFactory() {
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
    log.debug("OrgIdpDomainVerifierAuthenticatorFactory.action");
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.debug("OrgIdpDomainVerifierAuthenticatorFactory.authenticate");

    BrokeredIdentityContext brokerContext = PostOrgAuthFlow.getBrokeredIdentityContext(context);
    if (!PostOrgAuthFlow.brokeredIdpEnabled(context, brokerContext)) return;

    Map<String, String> idpConfig = brokerContext.getIdpConfig().getConfig();
    if (!idpConfig.containsKey(ORG_OWNER_CONFIG_KEY)) {
      log.debugf("OrgIdpDomainVerifierAuthenticator not compatible with flow");
      failChallenge(context, "incompatibleFlow");
      return;
    }

    var identityEmailDomain = Domains.extract(context.getUser().getEmail());
    if (identityEmailDomain.isEmpty()) {
      log.debugf(
          "User %s does not contain a valid email domain {}.",
          context.getUser(), context.getUser().getEmail());
      failChallenge(context, "emailDomainMissing");
      return;
    }

    var requireVerifiedDomain = requireVerifiedDomain(context.getAuthenticatorConfig());

    OrganizationProvider orgs = context.getSession().getProvider(OrganizationProvider.class);
    var orgIds = IdentityProviders.getAttributeMultivalued(idpConfig, ORG_OWNER_CONFIG_KEY);
    orgIds.stream()
        .map(orgId -> orgs.getOrganizationById(context.getRealm(), orgId))
        .filter(Objects::nonNull)
        .filter(org -> Domains.supportsDomain(org.getDomains(), identityEmailDomain.get()))
        .filter(org -> isVerifiedDomain(org, requireVerifiedDomain, identityEmailDomain.get()))
        .findFirst()
        .ifPresentOrElse(
            (value) -> context.success(),
            () -> {
              failChallenge(context, "validDomainNotFound");
              context
                  .getEvent()
                  .clone()
                  .user(brokerContext.getEmail())
                  .detail("orgIds", orgIds)
                  .error("User domain could not be found in the organizations with ids.");
            });
  }

  private boolean isVerifiedDomain(
      OrganizationModel org, boolean requireVerifiedDomain, String identityEmailDomain) {
    if (requireVerifiedDomain) {
      var domain = org.getDomain(identityEmailDomain);
      return domain.isVerified();
    }
    return true;
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
    return "Check if the organization owns the email domain of the user that logins via IdP. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Org IDP domain verifier";
  }

  @Override
  public String getReferenceCategory() {
    return "Post Broker";
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    factory.register(
        (ProviderEvent ev) -> {
          if (ev instanceof RealmModel.RealmPostCreateEvent) {
            PostOrgAuthFlow.realmPostCreate(
                (RealmModel.RealmPostCreateEvent) ev,
                PROVIDER_ID,
                AuthenticationExecutionModel.Requirement.DISABLED);
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
