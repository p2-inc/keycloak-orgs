package io.phasetwo.service.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.KeycloakOrgsAdminAPI;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.MountableFile;

/**
 * Base class for database-compatibility smoke tests. To add support for a new database, extend
 * this class, annotate it with {@code @TestInstance(Lifecycle.PER_CLASS)}, and implement the three
 * abstract methods. The inherited {@code @Test} will run automatically.
 *
 * <p>Tests only execute when {@code -Dinclude.integration=true} is set (activated by the
 * {@code integration-tests} Maven profile).
 */
@JBossLog
@EnabledIfSystemProperty(named = "include.integration", matches = "true")
abstract class AbstractDbCompatibilityTest {

  static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/phasetwo/keycloak-crdb:%s", System.getProperty("keycloak-version", "26.5.7"));
  static final String REALM = "master";
  static final String ADMIN_CLI = "admin-cli";

  static final String[] DEPS = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "org.keycloak:keycloak-admin-client",
    "io.phasetwo.keycloak:keycloak-events"
  };

  static List<File> getDeps() {
    List<File> deps = new ArrayList<>();
    for (String dep : DEPS) {
      deps.addAll(
          Maven.resolver()
              .loadPomFromFile("./pom.xml")
              .resolve(dep)
              .withoutTransitivity()
              .asList(File.class));
    }
    return deps;
  }

  // Instance fields — non-static so each concrete subclass owns its own containers.
  Network network;
  JdbcDatabaseContainer<?> db;
  KeycloakContainer keycloak;
  Keycloak adminClient;

  /**
   * Return a fully configured database container attached to {@code network}. Set the network alias
   * here; it must match what {@link #getDbInternalUrl()} uses.
   */
  protected abstract JdbcDatabaseContainer<?> createDbContainer(Network network);

  /** Value passed to Keycloak as {@code KC_DB} (e.g. {@code "mysql"}, {@code "mariadb"}). */
  protected abstract String getDbVendor();

  /**
   * JDBC URL that the Keycloak container will use to reach the database over the shared Docker
   * network (e.g. {@code "jdbc:mysql://mysql-db:3306/keycloak"}).
   */
  protected abstract String getDbInternalUrl();

  @BeforeAll
  void setUp() {
    network = Network.newNetwork();
    db = createDbContainer(network);
    db.start();
    keycloak = buildKeycloakContainer();
    keycloak.start();
    adminClient =
        Keycloak.getInstance(
            keycloak.getAuthServerUrl(),
            REALM,
            keycloak.getAdminUsername(),
            keycloak.getAdminPassword(),
            ADMIN_CLI);
  }

  @AfterAll
  void tearDown() throws IOException {
    if (keycloak != null) stopKeycloak(keycloak);
    if (db != null) db.stop();
    if (network != null) network.close();
  }

  @Test
  void createSearchDeleteOrganization() throws Exception {
    KeycloakOrgsAdminAPI orgsApi =
        new KeycloakOrgsAdminAPI(keycloak.getAuthServerUrl(), REALM, adminClient);

    OrganizationRepresentation created =
        orgsApi.createOrganization(new OrganizationRepresentation().name("smoke-test-org"));
    String orgId = created.getId();
    assertNotNull(orgId);
    assertThat(created.getName(), is("smoke-test-org"));

    List<OrganizationRepresentation> orgs = orgsApi.listOrganizations();
    assertThat(
        "created org must appear in listing",
        orgs.stream().anyMatch(o -> orgId.equals(o.getId())),
        is(true));

    given()
        .baseUri(keycloak.getAuthServerUrl())
        .basePath("realms/" + REALM + "/orgs")
        .contentType("application/json")
        .auth()
        .oauth2(adminClient.tokenManager().getAccessTokenString())
        .delete(orgId)
        .then()
        .statusCode(Status.NO_CONTENT.getStatusCode());

    orgs = orgsApi.listOrganizations();
    assertThat(
        "deleted org must not appear in listing",
        orgs.stream().noneMatch(o -> orgId.equals(o.getId())),
        is(true));
  }

  private KeycloakContainer buildKeycloakContainer() {
    KeycloakContainer kc =
        new KeycloakContainer(KEYCLOAK_IMAGE)
            .withNetwork(network)
            .withContextPath("/auth")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withProviderClassesFrom("target/classes")
            .withProviderLibsFrom(getDeps())
            .withEnv("KC_DB", getDbVendor())
            .withEnv("KC_DB_URL", getDbInternalUrl())
            .withEnv("KC_DB_USERNAME", db.getUsername())
            .withEnv("KC_DB_PASSWORD", db.getPassword());
    if (isJacocoPresent()) {
      kc =
          kc.withCopyFileToContainer(
                  MountableFile.forHostPath("target/jacoco-agent/"), "/jacoco-agent")
              .withEnv(
                  "JAVA_OPTS",
                  "-XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m"
                      + " -javaagent:/jacoco-agent/org.jacoco.agent-runtime.jar=destfile=/tmp/jacoco.exec");
    } else {
      kc = kc.withEnv("JAVA_OPTS", "-XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m");
    }
    return kc;
  }

  static boolean isJacocoPresent() {
    return Files.exists(Path.of("target/jacoco-agent/org.jacoco.agent-runtime.jar"));
  }

  static void stopKeycloak(KeycloakContainer kc) throws IOException {
    if (isJacocoPresent()) {
      String containerId = kc.getContainerId();
      String shortId = containerId.length() > 12 ? containerId.substring(0, 12) : containerId;
      kc.getDockerClient().stopContainerCmd(containerId).exec();
      Files.createDirectories(Path.of("target", "jacoco-report"));
      kc.copyFileFromContainer(
          "/tmp/jacoco.exec", "./target/jacoco-report/jacoco-%s.exec".formatted(shortId));
    }
    kc.stop();
  }
}
