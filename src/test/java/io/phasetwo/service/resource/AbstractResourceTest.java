package io.phasetwo.service.resource;

import io.phasetwo.client.OrganizationsResource;
import io.phasetwo.client.PhaseTwo;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.KeycloakSuite;
import java.util.List;
import org.junit.ClassRule;
import org.keycloak.admin.client.Keycloak;

public abstract class AbstractResourceTest {

  public static final String REALM = "master";

  @ClassRule public static KeycloakSuite server = KeycloakSuite.SERVER;

  public static PhaseTwo phaseTwo() {
    return phaseTwo(server.client());
  }

  public static PhaseTwo phaseTwo(Keycloak keycloak) {
    return new PhaseTwo(keycloak, server.getAuthUrl());
  }

  protected String createDefaultOrg(OrganizationsResource resource) {
    OrganizationRepresentation rep =
        new OrganizationRepresentation().name("example").domains(List.of("example.com"));
    return resource.create(rep);
  }
}
