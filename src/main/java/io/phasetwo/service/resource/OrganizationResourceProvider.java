package io.phasetwo.service.resource;

import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

/** */
@JBossLog
public class OrganizationResourceProvider extends BaseRealmResourceProvider {

  public OrganizationResourceProvider(KeycloakSession session) {
    super(session);
  }

  @Override
  protected Object getRealmResource() {
    RealmModel realm = session.getContext().getRealm();
    OrganizationsResource organization = new OrganizationsResource(realm);
    ResteasyProviderFactory.getInstance().injectProperties(organization);
    organization.setup();
    return organization;
  }
}
