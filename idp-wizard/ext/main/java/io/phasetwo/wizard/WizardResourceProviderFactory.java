package io.phasetwo.wizard;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@JBossLog
@AutoService(RealmResourceProviderFactory.class)
public class WizardResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "wizard";

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
    log.infof("Using override realm %s", override);
    return new WizardResourceProvider(session, override);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    KeycloakModelUtils.runJobInTransaction(factory, this::initClients);
    factory.register(
        (ProviderEvent event) -> {
          if (event instanceof RealmModel.RealmPostCreateEvent) {
            log.info("RealmPostCreateEvent");
            realmPostCreate((RealmModel.RealmPostCreateEvent) event);
          }
        });
  }

  private void initClients(KeycloakSession session) {
    try {
      for (RealmModel realm : session.realms().getRealms()) {
        createClient(realm, session);
      }
    } catch (Exception e) {
      log.warn(
          "Error initializing idp-wizard clients. Ignoring. You may have to create them manually.",
          e);
    }
  }

  private void realmPostCreate(RealmModel.RealmPostCreateEvent event) {
    RealmModel realm = event.getCreatedRealm();
    KeycloakSession session = event.getKeycloakSession();
    createClient(realm, session);
  }

  private void createClient(RealmModel realm, KeycloakSession session) {
    log.infof("Creating %s realm idp-wizard client.", realm.getName());
    ClientModel idpWizard = session.clients().getClientByClientId(realm, "idp-wizard");
    if (idpWizard == null) {
      log.infof("No idp-wizard client for %s realm. Creating...", realm.getName());
      if ("master".equals(realm.getName())) {
        idpWizard = createClientForMaster(realm, session);
      } else {
        idpWizard = createClientForRealm(realm, session);
      }
    }
    setClientScopeDefaults(realm, session, idpWizard);
  }

  private ClientModel createClientForRealm(RealmModel realm, KeycloakSession session) {
    String path = String.format("/realms/%s/wizard/", realm.getName());
    ClientModel idpWizard = session.clients().addClient(realm, "idp-wizard");
    setDefaults(idpWizard);
    idpWizard.setBaseUrl(path);
    idpWizard.setRedirectUris(ImmutableSet.of(String.format("%s*", path)));
    idpWizard.setWebOrigins(ImmutableSet.of("/*"));
    return idpWizard;
  }

  private ClientModel createClientForMaster(RealmModel realm, KeycloakSession session) {
    ClientModel idpWizard = session.clients().addClient(realm, "idp-wizard");
    setDefaults(idpWizard);
    idpWizard.setRedirectUris(ImmutableSet.of("/*"));
    idpWizard.setWebOrigins(ImmutableSet.of("/*"));
    return idpWizard;
  }

  private void setDefaults(ClientModel idpWizard) {
    idpWizard.setProtocol("openid-connect");
    idpWizard.setPublicClient(true);
    idpWizard.setRootUrl("${authBaseUrl}");
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
              log.infof("Found 'roles' client scope. Adding as default...");
              try {
                idpWizard.addClientScope(c, true);
              } catch (Exception e) {
                log.warn("'roles' client scope already exists. skipping...");
              }
            });
  }

  @Override
  public void close() {}
}
