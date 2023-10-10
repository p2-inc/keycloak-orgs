package io.phasetwo.wizard;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@JBossLog
@AutoService(RealmResourceProviderFactory.class)
public class WizardResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "wizard";
  public static final String NAME = "IdP Config Wizard";
  public static final String DESCRIPTION = "Wizards for configuring various vendor IdPs.";

  public static final String AUTH_REALM_OVERRIDE_CONFIG_KEY =
      "_providerConfig.wizard.auth-realm-override";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public RealmResourceProvider create(KeycloakSession session) {
    // get override config
    RealmModel realm = session.getContext().getRealm();
    String override =
        Optional.ofNullable(realm.getAttribute(AUTH_REALM_OVERRIDE_CONFIG_KEY))
            .orElse(realm.getName());
    if (!override.equals(realm.getName())) log.debugf("Using override realm %s", override);
    return new WizardResourceProvider(session, override);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    // KeycloakModelUtils.runJobInTransaction(factory, this::initClients);
    factory.register(
        (ProviderEvent event) -> {
          if (event instanceof RealmModel.RealmPostCreateEvent) {
            log.debug("RealmPostCreateEvent");
            realmPostCreate((RealmModel.RealmPostCreateEvent) event);
          }
        });
  }

  private void initClients(KeycloakSession session) {
    try {
      session
          .realms()
          .getRealmsStream()
          .forEach(
              realm -> {
                createClient(realm, session);
              });
    } catch (Exception e) {
      log.warnf(
          "Error initializing idp-wizard clients. Ignoring. You may have to create them manually. %s",
          e.getMessage());
    }
  }

  private void realmPostCreate(RealmModel.RealmPostCreateEvent event) {
    RealmModel realm = event.getCreatedRealm();
    KeycloakSession session = event.getKeycloakSession();
    createClient(realm, session);
  }

  private void createClient(RealmModel realm, KeycloakSession session) {
    log.debugf("Creating %s realm idp-wizard client.", realm.getName());
    ClientModel idpWizard = session.clients().getClientByClientId(realm, "idp-wizard");
    if (idpWizard == null) {
      log.debugf("No idp-wizard client for %s realm. Creating...", realm.getName());
      if ("master".equals(realm.getName())) {
        idpWizard = createClientForMaster(realm, session);
      } else {
        idpWizard = createClientForRealm(realm, session);
      }
    }
    setClientScopeDefaults(realm, session, idpWizard);
  }

  private String getRedirectPath(RealmModel realm) {
    return String.format("/realms/%s/%s/", realm.getName(), ID);
  }

  private ClientModel createClientForRealm(RealmModel realm, KeycloakSession session) {
    String path = getRedirectPath(realm);
    ClientModel idpWizard = session.clients().addClient(realm, "idp-wizard");
    setDefaults(realm, idpWizard);
    idpWizard.setRedirectUris(ImmutableSet.of(String.format("%s*", path)));
    idpWizard.setWebOrigins(ImmutableSet.of("/*"));
    idpWizard.setAttribute("post.logout.redirect.uris", "+");
    return idpWizard;
  }

  private ClientModel createClientForMaster(RealmModel realm, KeycloakSession session) {
    ClientModel idpWizard = session.clients().addClient(realm, "idp-wizard");
    setDefaults(realm, idpWizard);
    idpWizard.setRedirectUris(ImmutableSet.of("/*"));
    idpWizard.setWebOrigins(ImmutableSet.of("/*"));
    idpWizard.setAttribute("post.logout.redirect.uris", "+");
    return idpWizard;
  }

  private void setDefaults(RealmModel realm, ClientModel idpWizard) {
    idpWizard.setProtocol("openid-connect");
    idpWizard.setPublicClient(true);
    idpWizard.setRootUrl("${authBaseUrl}");
    idpWizard.setName(NAME);
    idpWizard.setDescription(DESCRIPTION);
    idpWizard.setBaseUrl(getRedirectPath(realm));
  }

  private void setOrganizationRoleMapper(ClientModel idpWizard) {
    ProtocolMapperModel pro =
        idpWizard.getProtocolMapperByName("openid-connect", "organizations");
    if (pro != null) {
      return;
    } else {
      pro = new ProtocolMapperModel();
    }
    pro.setProtocolMapper("oidc-organization-role-mapper");
    pro.setProtocol("openid-connect");
    pro.setName("organizations");
    Map<String, String> config =
        new ImmutableMap.Builder<String, String>()
            .put("id.token.claim", "true")
            .put("access.token.claim", "true")
            .put("claim.name", "organizations")
            .put("jsonType.label", "JSON")
            .put("userinfo.token.claim", "true")
            .build();
    pro.setConfig(config);
    //      "consentRequired": false,
    idpWizard.addProtocolMapper(pro);
  }

  private void setOrganizationIdMapper(ClientModel idpWizard) {
    ProtocolMapperModel pro =
        idpWizard.getProtocolMapperByName("openid-connect", "org_id");
    if (pro != null) {
      return;
    } else {
      pro = new ProtocolMapperModel();
    }
    pro.setProtocolMapper("oidc-usersessionmodel-note-mapper");
    pro.setProtocol("openid-connect");
    pro.setName("org_id");
    Map<String, String> config =
        new ImmutableMap.Builder<String, String>()
            .put("user.session.note", "org_id")
            .put("id.token.claim", "true")
            .put("access.token.claim", "true")
            .put("claim.name", "org_id")
            .put("jsonType.label", "String")
            .put("userinfo.token.claim", "true")
            .build();
    pro.setConfig(config);
    //      "consentRequired": false,
    idpWizard.addProtocolMapper(pro);
  }

  private void setClientScopeDefaults(
      RealmModel realm, KeycloakSession session, ClientModel idpWizard) {
    idpWizard.setFullScopeAllowed(true);
    session
        .clientScopes()
        .getClientScopesStream(realm)
        .filter(c -> (c.getRealm().equals(realm) && c.getName().equals("roles")))
        .forEach(
            c -> {
              log.debugf("Found 'roles' client scope. Adding as default...");
              try {
                idpWizard.addClientScope(c, true);
              } catch (Exception e) {
                log.warn("'roles' client scope already exists. skipping...");
              }
            });
    setOrganizationRoleMapper(idpWizard);
    setOrganizationIdMapper(idpWizard);
  }

  @Override
  public void close() {}
}
