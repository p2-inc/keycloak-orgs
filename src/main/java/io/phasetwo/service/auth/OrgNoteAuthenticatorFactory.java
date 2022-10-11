package io.phasetwo.service.auth;

import static io.phasetwo.service.Orgs.*;

import com.google.auto.service.AutoService;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;

/** */
@JBossLog
@AutoService(AuthenticatorFactory.class)
public class OrgNoteAuthenticatorFactory extends BaseAuthenticatorFactory {

  public static final String PROVIDER_ID = "ext-auth-org-note";

  public OrgNoteAuthenticatorFactory() {
    super(PROVIDER_ID);
  }

  @Override
  public Authenticator create(KeycloakSession session) {
    return new IdpAuthenticator() {
      @Override
      public void authenticateImpl​(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        log.info("OrgNoteAuthenticatorFactory.authenticate");
        context.attempted();
        if (!PostOrgAuthFlow.brokeredIdpEnabled(context, brokerContext)) return;
        Map<String, String> idpConfig = brokerContext.getIdpConfig().getConfig();
        if (idpConfig != null && idpConfig.containsKey(ORG_OWNER_CONFIG_KEY)) {
          log.infof(
              "Set auth note %s = %s for IdP %s",
              FIELD_ORG_ID,
              idpConfig.get(ORG_OWNER_CONFIG_KEY),
              brokerContext.getIdpConfig().getAlias());
          context
              .getAuthenticationSession()
              .setAuthNote(FIELD_ORG_ID, idpConfig.get(ORG_OWNER_CONFIG_KEY));
        } else {
          log.infof("No organization owns IdP %s", brokerContext.getIdpConfig().getAlias());
        }
      }
    };
  }

  @Override
  public boolean isUserSetupAllowed() {
    return false;
  }

  @Override
  public String getHelpText() {
    return "Sets an auth note of the org_id if an organization-owned IdP was used to log in. Use only in Post Login Flows.";
  }

  @Override
  public String getDisplayType() {
    return "Org To Auth Note";
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
            PostOrgAuthFlow.realmPostCreate((RealmModel.RealmPostCreateEvent) ev, PROVIDER_ID);
          }
        });
  }
}
