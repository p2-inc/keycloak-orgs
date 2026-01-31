package io.phasetwo.wizard;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.models.AuthenticationFlowModel;
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
  public static final String CONFIG_CLIENT_ID = "idp-wizard";
  public static final String CONFIG_NAME = "IdP Config Wizard";
  public static final String CONFIG_DESCRIPTION = "Wizards for configuring various vendor IdPs.";
  public static final String TESTER_CLIENT_ID = "idp-tester";
  public static final String TESTER_NAME = "IdP Tester";
  public static final String TESTER_DESCRIPTION = "Testing for validating vendor IdPs.";

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
                createClients(realm, session);
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
    createClients(realm, session);
  }

  private void createClients(RealmModel realm, KeycloakSession session) {
    createIdpWizardClient(realm, session);
    createIdpTesterClient(realm, session);
  }

  private void createIdpWizardClient(RealmModel realm, KeycloakSession session) {
    log.debugf("Creating %s realm idp-wizard client.", realm.getName());
    ClientModel idpWizard = session.clients().getClientByClientId(realm, CONFIG_CLIENT_ID);
    if (idpWizard == null) {
      log.debugf("No idp-wizard client for %s realm. Creating...", realm.getName());
      if ("master".equals(realm.getName())) {
        idpWizard =
            createClientForMaster(
                realm, session, CONFIG_CLIENT_ID, CONFIG_NAME, CONFIG_DESCRIPTION);
      } else {
        idpWizard =
            createClientForRealm(realm, session, CONFIG_CLIENT_ID, CONFIG_NAME, CONFIG_DESCRIPTION);
      }
    }
    setClientScopeDefaults(realm, session, idpWizard);
  }

  private void createIdpTesterClient(RealmModel realm, KeycloakSession session) {
    log.debugf("Creating %s realm idp-tester client.", realm.getName());
    ClientModel idpTester = session.clients().getClientByClientId(realm, TESTER_CLIENT_ID);
    if (idpTester == null) {
      log.debugf("No idp-tester client for %s realm. Creating...", realm.getName());
      if ("master".equals(realm.getName())) {
        idpTester =
            createClientForMaster(
                realm, session, TESTER_CLIENT_ID, TESTER_NAME, TESTER_DESCRIPTION);
      } else {
        idpTester =
            createClientForRealm(realm, session, TESTER_CLIENT_ID, TESTER_NAME, TESTER_DESCRIPTION);
      }
    }
    setAuthFlow(realm, idpTester, "browser", "idp validate");
  }

  private String getRedirectPath(RealmModel realm) {
    return String.format("/realms/%s/%s/", realm.getName(), ID);
  }

  private ClientModel createClientForRealm(
      RealmModel realm, KeycloakSession session, String clientId, String name, String description) {
    String path = getRedirectPath(realm);
    ClientModel client = session.clients().addClient(realm, clientId);
    setDefaults(realm, client, name, description);
    client.setRedirectUris(ImmutableSet.of(String.format("%s*", path)));
    client.setWebOrigins(ImmutableSet.of("/*"));
    client.setAttribute("post.logout.redirect.uris", "+");
    return client;
  }

  private ClientModel createClientForMaster(
      RealmModel realm, KeycloakSession session, String clientId, String name, String description) {
    ClientModel client = session.clients().addClient(realm, clientId);
    setDefaults(realm, client, name, description);
    ;
    client.setRedirectUris(ImmutableSet.of("/*"));
    client.setWebOrigins(ImmutableSet.of("/*"));
    client.setAttribute("post.logout.redirect.uris", "+");
    return client;
  }

  private void setDefaults(RealmModel realm, ClientModel client, String name, String description) {
    client.setProtocol("openid-connect");
    client.setPublicClient(true);
    client.setRootUrl("${authBaseUrl}");
    client.setName(name);
    client.setDescription(description);
    client.setBaseUrl(getRedirectPath(realm));
  }

  private void setOrganizationRoleMapper(ClientModel client) {
    ProtocolMapperModel pro = client.getProtocolMapperByName("openid-connect", "organizations");
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
    client.addProtocolMapper(pro);
  }

  private void setOrganizationIdMapper(ClientModel client) {
    ProtocolMapperModel pro = client.getProtocolMapperByName("openid-connect", "org_id");
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
    client.addProtocolMapper(pro);
  }

  private void setClientScopeDefaults(
      RealmModel realm, KeycloakSession session, ClientModel client) {
    client.setFullScopeAllowed(true);
    session
        .clientScopes()
        .getClientScopesStream(realm)
        .filter(c -> (c.getRealm().equals(realm) && c.getName().equals("roles")))
        .forEach(
            c -> {
              log.debugf("Found 'roles' client scope. Adding as default...");
              try {
                client.addClientScope(c, true);
              } catch (Exception e) {
                log.warn("'roles' client scope already exists. skipping...");
              }
            });
    setOrganizationRoleMapper(client);
    setOrganizationIdMapper(client);
  }

  private void setAuthFlow(RealmModel realm, ClientModel client, String binding, String flowAlias) {
    AuthenticationFlowModel flow = realm.getFlowByAlias(flowAlias);
    if (flow != null) {
      log.infof("Flow for %s %s %s", flowAlias, flow.getId(), binding);
      client.setAuthenticationFlowBindingOverride(binding, flow.getId());
    } else {
      log.warnf("Flow for %s alias doesn't exist. Skipping assiging to %s", flowAlias, binding);
    }
  }

  @Override
  public void close() {}
}
