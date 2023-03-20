package io.phasetwo.portal;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
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
public class PortalResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "portal";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public RealmResourceProvider create(KeycloakSession session) {
    // get override config
    RealmModel realm = session.getContext().getRealm();
    String override = realm.getName();
    return new PortalResourceProvider(session, override);
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
          "Error initializing admin-portal clients. Ignoring. You may have to create them manually. %s",
          e.getMessage());
    }
  }

  private void realmPostCreate(RealmModel.RealmPostCreateEvent event) {
    RealmModel realm = event.getCreatedRealm();
    KeycloakSession session = event.getKeycloakSession();
    createClient(realm, session);
  }

  private void createClient(RealmModel realm, KeycloakSession session) {
    log.debugf("Creating %s realm admin-portal client.", realm.getName());
    ClientModel adminPortal = session.clients().getClientByClientId(realm, "admin-portal");
    if (adminPortal == null) {
      log.debugf("No admin-portal client for %s realm. Creating...", realm.getName());
      if ("master".equals(realm.getName())) {
        adminPortal = createClientForMaster(realm, session);
      } else {
        adminPortal = createClientForRealm(realm, session);
      }
      updateAccountConsoleForRealm(realm, session);
    }
    setClientScopeDefaults(realm, session, adminPortal);
  }

  private String getRedirectPath(RealmModel realm) {
    return String.format("/realms/%s/portal/", realm.getName());
  }

  private ClientModel createClientForRealm(RealmModel realm, KeycloakSession session) {
    String path = getRedirectPath(realm);
    ClientModel adminPortal = session.clients().addClient(realm, "admin-portal");
    setDefaults(adminPortal);
    adminPortal.setBaseUrl(path);
    adminPortal.setRedirectUris(ImmutableSet.of(String.format("%s*", path)));
    adminPortal.setWebOrigins(ImmutableSet.of("/*"));
    adminPortal.setAttribute("post.logout.redirect.uris", "+");
    return adminPortal;
  }

  private void updateAccountConsoleForRealm(RealmModel realm, KeycloakSession session) {
    ClientModel accountConsole = session.clients().getClientByClientId(realm, "account-console");
    accountConsole.addRedirectUri(String.format("%s*", getRedirectPath(realm)));
  }

  private ClientModel createClientForMaster(RealmModel realm, KeycloakSession session) {
    ClientModel adminPortal = session.clients().addClient(realm, "admin-portal");
    setDefaults(adminPortal);
    adminPortal.setRedirectUris(ImmutableSet.of("/*"));
    adminPortal.setWebOrigins(ImmutableSet.of("/*"));
    adminPortal.setAttribute("post.logout.redirect.uris", "+");
    return adminPortal;
  }

  private void setDefaults(ClientModel adminPortal) {
    adminPortal.setProtocol("openid-connect");
    adminPortal.setPublicClient(true);
    adminPortal.setRootUrl("${authBaseUrl}");
  }

  private void setOrganizationRoleMapper(ClientModel adminPortal) {
    ProtocolMapperModel pro = new ProtocolMapperModel();
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
    adminPortal.addProtocolMapper(pro);
  }

  private void setOrganizationIdMapper(ClientModel adminPortal) {
    ProtocolMapperModel pro = new ProtocolMapperModel();
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
    adminPortal.addProtocolMapper(pro);
  }

  private void setClientScopeDefaults(
      RealmModel realm, KeycloakSession session, ClientModel adminPortal) {
    adminPortal.setFullScopeAllowed(true);
    session
        .clientScopes()
        .getClientScopesStream(realm)
        .filter(c -> (c.getRealm().equals(realm) && c.getName().equals("roles")))
        .forEach(
            c -> {
              log.debugf("Found 'roles' client scope. Adding as default...");
              try {
                adminPortal.addClientScope(c, true);
              } catch (Exception e) {
                log.warn("'roles' client scope already exists. skipping...");
              }
            });
    setOrganizationRoleMapper(adminPortal);
    setOrganizationIdMapper(adminPortal);
  }

  @Override
  public void close() {}
}
