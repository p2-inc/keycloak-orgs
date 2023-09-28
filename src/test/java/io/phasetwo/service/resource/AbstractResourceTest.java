package io.phasetwo.service.resource;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.OrganizationsResource;
import io.phasetwo.client.PhaseTwo;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractResourceTest {

  static final String[] deps = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "org.keycloak:keycloak-admin-client"
  };

  static List<File> getDeps() {
    List<File> dependencies = new ArrayList<File>();
    for (String dep : deps) {
      dependencies.addAll(getDep(dep));
    }
    return dependencies;
  }

  static List<File> getDep(String pkg) {
    List<File> dependencies =
        Maven.resolver()
            .loadPomFromFile("./pom.xml")
            .resolve(pkg)
            .withoutTransitivity()
            .asList(File.class);
    return dependencies;
  }

  @Container
  public static final KeycloakContainer container =
      new KeycloakContainer("quay.io/phasetwo/keycloak-crdb:22.0.3")
          .withContextPath("/auth")
          .withReuse(true)
          .withProviderClassesFrom("target/classes")
          .withProviderLibsFrom(getDeps());

  @BeforeAll
  public static void beforeAll() {
    container.start();
  }

  @AfterAll
  public static void afterAll() {
    container.stop();
  }

  public static Keycloak getKeycloak() {
    return container.getKeycloakAdminClient();
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    return Keycloak.getInstance(getAuthUrl(), realm, user, pass, clientId);
  }

  public static String getAuthUrl() {
    return container.getAuthServerUrl();
  }

  public static final String REALM = "master";

  public static PhaseTwo phaseTwo() {
    return phaseTwo(getKeycloak());
  }

  public static PhaseTwo phaseTwo(Keycloak keycloak) {
    return new PhaseTwo(keycloak, getAuthUrl());
  }

  protected String createDefaultOrg(OrganizationsResource resource) {
    OrganizationRepresentation rep =
        new OrganizationRepresentation().name("example").domains(List.of("example.com"));
    return resource.create(rep);
  }
}
