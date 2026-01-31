package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.ORG_CONFIG_VALIDATE_IDP_KEY;
import static io.phasetwo.service.Orgs.ORG_VALIDATION_PENDING_CONFIG_KEY;

import com.google.auto.service.AutoService;
import java.util.Map;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class ValidateIdpAuthenticatorFactory extends BaseAuthenticatorFactory
    implements DefaultAuthenticator {

  public static final String PROVIDER_ID = "ext-auth-validate-idp";
  private static final String IDP_VALIATION_FORM = "idp-validation.ftl";

  public ValidateIdpAuthenticatorFactory() {
    super(PROVIDER_ID);
  }

  @Override
  public void authenticate(AuthenticationFlowContext context) {
    log.debug("ValidateIdpAuthenticatorFactory.authenticate");
    validateIdp(context);
  }

  @Override
  public void action(AuthenticationFlowContext context) {
    log.debug("ValidateIdpAuthenticatorFactory.action");
  }

  private void validateIdp(AuthenticationFlowContext context) {
    PostOrgAuthFlow.setStatus(context);
    BrokeredIdentityContext brokerContext = PostOrgAuthFlow.getBrokeredIdentityContext(context);
    if (!PostOrgAuthFlow.brokeredIdpEnabled(context, brokerContext)) return;

    IdentityProviderModel idp = brokerContext.getIdpConfig();
    Map<String, String> idpConfig = idp.getConfig();
    boolean validationPending =
        Optional.ofNullable(idpConfig)
            .map(config -> Boolean.parseBoolean(config.get(ORG_VALIDATION_PENDING_CONFIG_KEY)))
            .orElse(false);
    boolean validateIdpEnabled =
        context.getRealm().getAttribute(ORG_CONFIG_VALIDATE_IDP_KEY, false);

    if (!validationPending || !validateIdpEnabled) {
      return;
    }

    if (idpConfig != null) {
      idpConfig.remove(ORG_VALIDATION_PENDING_CONFIG_KEY);
    }
    context.getSession().identityProviders().update(idp);

    String firstName = Optional.ofNullable(brokerContext.getFirstName()).orElse("");
    String lastName = Optional.ofNullable(brokerContext.getLastName()).orElse("");
    String email = Optional.ofNullable(brokerContext.getEmail()).orElse("");
    String message =
        String.format(
            "Successful validation. You logged in as %s %s %s. You can close this window now.",
            firstName, lastName, email);
    context.challenge(context.form().setInfo(message).createForm(IDP_VALIATION_FORM));
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
    return "Validates newly created organization identity providers. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Validate IdP";
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
            IdpValidateAuthFlow.realmPostCreate((RealmModel.RealmPostCreateEvent) ev);
            PostOrgAuthFlow.realmPostCreate(
                (RealmModel.RealmPostCreateEvent) ev,
                PROVIDER_ID,
                AuthenticationExecutionModel.Requirement.REQUIRED,
                new Integer(0));
          }
        });
  }
}
