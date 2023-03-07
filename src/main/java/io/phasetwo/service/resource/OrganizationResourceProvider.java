package io.phasetwo.service.resource;

import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

/** */
@JBossLog
public class OrganizationResourceProvider extends BaseRealmResourceProvider {

  public OrganizationResourceProvider(KeycloakSession session) {
    super(session);
  }

  @Override
  protected Object getRealmResource() {
    OrganizationsResource organization = new OrganizationsResource(session);
    organization.setup();
    return organization;
  }
}
