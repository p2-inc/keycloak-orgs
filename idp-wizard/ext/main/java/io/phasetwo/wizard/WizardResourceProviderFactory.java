package io.phasetwo.wizard;

import com.google.auto.service.AutoService;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;
import java.util.Optional;
import org.keycloak.models.RealmModel;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@AutoService(RealmResourceProviderFactory.class)
public class WizardResourceProviderFactory implements RealmResourceProviderFactory {

  public static final String ID = "wizard";

  public static final String AUTH_REALM_OVERRIDE_CONFIG_KEY = "_providerConfig.wizard.auth-realm-override";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public RealmResourceProvider create(KeycloakSession session) {
    // get override config
    RealmModel realm = session.getContext().getRealm();
    String override = Optional.of(realm.getAttribute(AUTH_REALM_OVERRIDE_CONFIG_KEY)).orElse(realm.getName());
    log.infof("Using override realm %s", override);
    return new WizardResourceProvider(session, override);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
