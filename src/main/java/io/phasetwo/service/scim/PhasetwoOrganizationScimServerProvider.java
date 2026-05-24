package io.phasetwo.service.scim;

import fi.metatavu.keycloak.scim.server.config.ConfigurationError;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimContext;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimServer;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimServerProvider;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.scim.spi.ScimConfigurationProvider;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import java.net.URI;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;

public class PhasetwoOrganizationScimServerProvider implements OrganizationScimServerProvider {

  protected final KeycloakSession session;

  public PhasetwoOrganizationScimServerProvider(KeycloakSession session) {
    this.session = session;
  }

  public boolean organizationExists(String orgId) {
    RealmModel realm = session.getContext().getRealm();
    OrganizationProvider orgProvider = session.getProvider(OrganizationProvider.class);
    return orgProvider.getOrganizationById(realm, orgId) != null;
  }

  @Override
  public OrganizationScimServer getScimServer(KeycloakSession session) {
    return new OrganizationScimServer() {
      @Override
      public OrganizationScimContext getScimContext(
          KeycloakSession session, String organizationId) {
        return createScimContext(session, organizationId);
      }
    };
  }

  public static final String PHASETWO_ORGANIZATION_SESSION_ATTRIBUTE = "ext-phasetwo-organization";

  private static OrganizationScimContext createScimContext(
      KeycloakSession session, String organizationId) {
    RealmModel realm = session.getContext().getRealm();
    if (realm == null) {
      throw new NotFoundException("Realm not found");
    }

    final OrganizationModel organization =
        session.getProvider(OrganizationProvider.class).getOrganizationById(realm, organizationId);

    if (organization == null) {
      throw new NotFoundException("Organization not found");
    }

    session.setAttribute(PHASETWO_ORGANIZATION_SESSION_ATTRIBUTE, organization);

    URI baseUri =
        session
            .getContext()
            .getUri()
            .getBaseUri()
            .resolve(
                String.format(
                    "realms/%s/scim/v2/organizations/%s/", realm.getName(), organization.getId()));

    ScimConfigurationProvider configProvider =
        session.getProvider(ScimConfigurationProvider.class);
    ComponentScimConfig config = configProvider.getConfiguration(organizationId);
    if (config == null) {
      throw new NotFoundException(organizationId + " has no SCIM configuration");
    }
    if (!config.isEnabled()) {
      throw new NotFoundException(organizationId + " SCIM configuration is currently disabled");
    }

    try {
      config.validateConfig();
    } catch (ConfigurationError e) {
      throw new InternalServerErrorException("Invalid SCIM configuration", e);
    }

    return new PhasetwoOrganizationScimContext(baseUri, session, realm, config, organization);
  }
}
