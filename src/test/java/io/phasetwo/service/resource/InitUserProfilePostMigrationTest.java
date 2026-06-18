package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.web.JbossLogConsumer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.MountableFile;

@JBossLog
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
public class InitUserProfilePostMigrationTest {

  static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/phasetwo/keycloak-crdb:%s", System.getProperty("keycloak-version", "26.5.7"));
  static final String REALM = "master";
  static final String ADMIN_CLI = "admin-cli";

  static final String[] deps = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "org.keycloak:keycloak-admin-client",
    "io.phasetwo.keycloak:keycloak-events"
  };

  static List<File> getDeps() {
    List<File> dependencies = new ArrayList<>();
    for (String dep : deps) {
      dependencies.addAll(getDep(dep));
    }
    return dependencies;
  }

  static List<File> getDep(String pkg) {
    return Maven.resolver()
        .loadPomFromFile("./pom.xml")
        .resolve(pkg)
        .withoutTransitivity()
        .asList(File.class);
  }

  static Network network;
  static PostgreSQLContainer<?> postgres;
  static KeycloakContainer keycloak;
  static Keycloak adminClient;

  @BeforeAll
  static void setUp() throws IOException {
    network = Network.newNetwork();

    postgres =
        new PostgreSQLContainer<>("postgres:17")
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withDatabaseName("keycloak")
            .withUsername("keycloak")
            .withPassword("keycloak");
    postgres.start();

    keycloak = buildKeycloakContainer();
    keycloak.start();
    adminClient = buildAdminClient();
  }

  @AfterAll
  static void tearDown() throws IOException {
    if (keycloak != null) AbstractDbCompatibilityTest.stopKeycloak(keycloak);
    if (postgres != null) postgres.stop();
    if (network != null) network.close();
  }

  private static KeycloakContainer buildKeycloakContainer() {
    return buildKeycloakContainer(Map.of());
  }

  private static KeycloakContainer buildKeycloakContainer(Map<String, String> additionalEnvvars) {
    KeycloakContainer kc =
        new KeycloakContainer(KEYCLOAK_IMAGE)
            .withNetwork(network)
            .withContextPath("/auth")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withProviderClassesFrom("target/classes")
            .withProviderLibsFrom(getDeps())
            .withEnv("KC_DB", "postgres")
            .withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/keycloak")
            .withEnv("KC_DB_USERNAME", "keycloak")
            .withEnv("KC_DB_PASSWORD", "keycloak")
            .withLogConsumer(new JbossLogConsumer(log));
    additionalEnvvars.forEach(kc::withEnv);
    if (AbstractDbCompatibilityTest.isJacocoPresent()) {
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

  private static Keycloak buildAdminClient() {
    return Keycloak.getInstance(
        keycloak.getAuthServerUrl(),
        REALM,
        keycloak.getAdminUsername(),
        keycloak.getAdminPassword(),
        ADMIN_CLI);
  }

  private static void restartKeycloak() throws IOException {
    restartKeycloak(Map.of());
  }

  private static void restartKeycloak(Map<String, String> additionalEnvvars) throws IOException {
    AbstractDbCompatibilityTest.stopKeycloak(keycloak);
    keycloak = buildKeycloakContainer(additionalEnvvars);
    keycloak.start();
    adminClient = buildAdminClient();
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private void createRealm(String realmName) {
    RealmRepresentation rep = new RealmRepresentation();
    rep.setRealm(realmName);
    rep.setEnabled(true);
    adminClient.realms().create(rep);
  }

  private void removeActiveOrgAttribute(String realmName) {
    var userProfileResource = adminClient.realm(realmName).users().userProfile();
    UPConfig config = userProfileResource.getConfiguration();
    config.getAttributes().removeIf(a -> ACTIVE_ORGANIZATION.equals(a.getName()));
    userProfileResource.update(config);
  }

  private void assertRealmHasActiveOrgAttribute(String realmName) {
    assertActiveOrgAttributeCount(realmName, 1);
  }

  private void assertRealmMissingActiveOrgAttribute(String realmName) {
    assertActiveOrgAttributeCount(realmName, 0);
  }

  private void assertActiveOrgAttributeCount(String realmName, int expectedCount) {
    UPConfig config = adminClient.realm(realmName).users().userProfile().getConfiguration();
    long count =
        config.getAttributes().stream()
            .filter(a -> ACTIVE_ORGANIZATION.equals(a.getName()))
            .count();
    assertThat(
        "active_organization attribute count in realm '" + realmName + "'",
        (int) count,
        is(expectedCount));
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  @Test
  void testAttributeIsRestoredByMigration() throws Exception {
    // Create two extra realms while Keycloak is running.
    // RealmPostCreateEvent fires and adds active_organization immediately.
    String realmA = "up-restore-a";
    String realmB = "up-restore-b";
    createRealm(realmA);
    createRealm(realmB);

    assertRealmHasActiveOrgAttribute(REALM);
    assertRealmHasActiveOrgAttribute(realmA);
    assertRealmHasActiveOrgAttribute(realmB);

    // Remove the attribute to simulate realms that pre-date the plugin.
    removeActiveOrgAttribute(realmA);
    removeActiveOrgAttribute(realmB);
    assertRealmMissingActiveOrgAttribute(realmA);
    assertRealmMissingActiveOrgAttribute(realmB);

    // Restart → PostMigrationEvent → postMigrationInitUserProfile must restore it.
    restartKeycloak();

    assertRealmHasActiveOrgAttribute(REALM);
    assertRealmHasActiveOrgAttribute(realmA);
    assertRealmHasActiveOrgAttribute(realmB);
  }

  @Test
  void testMigrationIsIdempotent() throws Exception {
    String realmA = "up-idempotent-a";
    createRealm(realmA);

    // Attribute is added by RealmPostCreateEvent on creation.
    assertActiveOrgAttributeCount(realmA, 1);

    // First restart: postMigrationInitUserProfile runs; attribute already present.
    restartKeycloak();
    assertActiveOrgAttributeCount(realmA, 1);

    // Second restart: still exactly one copy — no duplicates.
    restartKeycloak();
    assertActiveOrgAttributeCount(realmA, 1);
  }

  @Test
  void testMultiRealmMigration() throws Exception {
    List<String> realms = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      String realm = "up-multi-" + i;
      createRealm(realm);
      realms.add(realm);
    }

    // Strip the attribute from every realm to simulate a pre-plugin state.
    for (String realm : realms) {
      removeActiveOrgAttribute(realm);
    }
    for (String realm : realms) {
      assertRealmMissingActiveOrgAttribute(realm);
    }

    // Restart → all 5 realms must have the attribute restored.
    restartKeycloak();

    for (String realm : realms) {
      assertRealmHasActiveOrgAttribute(realm);
    }

    // Second restart: idempotency — no duplicates across any realm.
    restartKeycloak();
    for (String realm : realms) {
      assertActiveOrgAttributeCount(realm, 1);
    }
  }

  @Test
  void testMigrationCompletesSuccessfullyAndLogsCompletion() throws Exception {
    // All realms already have the attribute before this restart, so
    // postMigrationInitUserProfile should skip all writes.
    // After the fix, it must emit a completion log line (analogous to
    // "Organization role migration complete" in OrganizationResourceProviderFactory)
    // so that the no-write path is observable in tests and in production logs.
    String realmA = "up-log-a";
    createRealm(realmA);

    assertRealmHasActiveOrgAttribute(REALM);
    assertRealmHasActiveOrgAttribute(realmA);

    restartKeycloak();

    assertThat(
        "postMigrationInitUserProfile should log completion",
        keycloak.getLogs().contains("User profile migration complete"),
        is(true));

    // Functional correctness still holds after a no-write restart.
    assertRealmHasActiveOrgAttribute(REALM);
    assertRealmHasActiveOrgAttribute(realmA);
  }

  @Test
  void testMigrationSkippedWhenKcOrgsSkipMigrationSet() throws Exception {
    // KC_ORGS_SKIP_MIGRATION suppresses postMigrationInitUserProfile entirely,
    // mirroring the same guard used by OrganizationResourceProviderFactory#initRoles.
    String realmA = "up-skip-env-a";
    createRealm(realmA);
    removeActiveOrgAttribute(realmA);
    assertRealmMissingActiveOrgAttribute(realmA);

    restartKeycloak(Map.of("KC_ORGS_SKIP_MIGRATION", "true"));

    // Keycloak must start cleanly when migration is suppressed.
    assertThat(
        "Keycloak should be reachable when migration is skipped",
        adminClient.realms().findAll().isEmpty(),
        is(false));

    // The attribute must NOT have been restored — migration was skipped.
    assertRealmMissingActiveOrgAttribute(realmA);
  }
}
