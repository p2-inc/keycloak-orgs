package io.phasetwo.service.scim;

import com.google.auto.service.AutoService;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimServerProvider;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimServerProviderFactory;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

@AutoService(OrganizationScimServerProviderFactory.class)
public class PhasetwoOrganizationScimServerProviderFactory
    implements OrganizationScimServerProviderFactory {

  public static final String PROVIDER_ID = "default";

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public OrganizationScimServerProvider create(KeycloakSession session) {
    return new PhasetwoOrganizationScimServerProvider(session);
  }

  @Override
  public void init(Scope config) {}

  @Override
  public void postInit(KeycloakSessionFactory factory) {}

  @Override
  public void close() {}
}
