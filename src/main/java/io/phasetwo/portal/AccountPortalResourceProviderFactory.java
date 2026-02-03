package io.phasetwo.portal;

import com.google.auto.service.AutoService;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.services.resource.AccountResourceProvider;
import org.keycloak.services.resource.AccountResourceProviderFactory;

@JBossLog
@AutoService(AccountResourceProviderFactory.class)
public class AccountPortalResourceProviderFactory implements AccountResourceProviderFactory {

  public static final String ID = "portal";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public AccountResourceProvider create(KeycloakSession session) {
    // get override config
    RealmModel realm = session.getContext().getRealm();
    String override = realm.getName();
    return new PortalResourceProvider(session, override);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {
    factory.register(
        (ProviderEvent event) -> {
          if (event instanceof RealmModel.RealmPostCreateEvent) {
            log.debug("RealmPostCreateEvent");
            realmPostCreate((RealmModel.RealmPostCreateEvent) event);
          }
        });
  }

  private void realmPostCreate(RealmModel.RealmPostCreateEvent event) {
    // no-op for now
  }

  @Override
  public void close() {}
}
