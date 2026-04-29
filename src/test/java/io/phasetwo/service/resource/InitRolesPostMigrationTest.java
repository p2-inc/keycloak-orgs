package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.OrganizationAdminAuth.ORG_ROLE_DELETE_ORGANIZATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.KeycloakOrgsAdminAPI;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.MountableFile;

@JBossLog
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
public class InitRolesPostMigrationTest {

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
  static void setUp() {
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
    if (keycloak != null) stopKeycloak(keycloak);
    if (postgres != null) postgres.stop();
    if (network != null) network.close();
  }

  private static KeycloakContainer buildKeycloakContainer() {
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
            .withEnv("KC_DB_PASSWORD", "keycloak");
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

  // Gracefully stops the container so the JVM flushes Jacoco data, then extracts the report.
  // Each container gets its own .exec file (keyed by short container ID); the maven jacoco:merge
  // goal combines all .exec files in target/jacoco-report/ into a single report.
  private static void stopKeycloak(KeycloakContainer kc) throws IOException {
    AbstractDbCompatibilityTest.stopKeycloak(kc);
  }

  private static void restartKeycloak() throws IOException {
    stopKeycloak(keycloak);
    keycloak = buildKeycloakContainer();
    keycloak.start();
    adminClient = buildAdminClient();
  }

  private KeycloakOrgsAdminAPI orgsApi() {
    return new KeycloakOrgsAdminAPI(keycloak.getAuthServerUrl(), REALM, adminClient);
  }

  private KeycloakOrgsAdminAPI orgsApi(String realm) {
    return new KeycloakOrgsAdminAPI(keycloak.getAuthServerUrl(), realm, adminClient);
  }

  private static void restartKeycloakWithBatchSize(int batchSize) throws IOException {
    stopKeycloak(keycloak);
    keycloak =
        buildKeycloakContainer()
            .withEnv("KC_ORGS_MIGRATION_BATCH_SIZE", String.valueOf(batchSize));
    keycloak.start();
    adminClient = buildAdminClient();
  }

  private List<String> createOrgsInRealm(String realm, int count) throws IOException {
    List<String> ids = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      OrganizationRepresentation org =
          orgsApi(realm)
              .createOrganization(new OrganizationRepresentation().name("test-org-" + i));
      ids.add(org.getId());
    }
    return ids;
  }

  private void assertAllOrgsHaveRole(String realm, List<String> orgIds, String roleName)
      throws IOException {
    for (String orgId : orgIds) {
      orgsApi(realm).assertOrgHasRole(orgId, roleName);
    }
  }

  @Test
  void testUpdateExistingOrganizationsWithNewRoles() throws Exception {
    int orgCount = 3;
    List<String> orgIds = new ArrayList<>();
    for (int i = 0; i < orgCount; i++) {
      OrganizationRepresentation org =
          orgsApi()
              .createOrganization(
                  new OrganizationRepresentation().name("migration-test-org-" + i));
      orgIds.add(org.getId());
    }

    // Organizations get delete-organization by default (added since PR #331)
    for (String orgId : orgIds) {
      orgsApi().assertOrgHasRole(orgId, ORG_ROLE_DELETE_ORGANIZATION);
    }

    // The REST API protects default roles from deletion, so we remove the role
    // directly from the DB to simulate organizations that existed before PR #331
    deleteOrgRoleFromDb(orgIds, ORG_ROLE_DELETE_ORGANIZATION);
    assertDbRoleCount(orgIds, ORG_ROLE_DELETE_ORGANIZATION, 0);

    // Restart triggers PostMigrationEvent → initRoles → updateExistingOrganizationsWithNewRoles
    restartKeycloak();

    assertDbRoleCount(orgIds, ORG_ROLE_DELETE_ORGANIZATION, orgCount);
    for (String orgId : orgIds) {
      orgsApi().assertOrgHasRole(orgId, ORG_ROLE_DELETE_ORGANIZATION);
    }

    // Second restart verifies idempotency — no duplicate roles
    restartKeycloak();
    assertDbRoleCount(orgIds, ORG_ROLE_DELETE_ORGANIZATION, orgCount);
  }

  @Test
  void testMultiRealmMigrationWithBatching() throws Exception {
    // Create 3 additional realms alongside master
    String realmA = "migration-realm-a";
    String realmB = "migration-realm-b";
    String realmC = "migration-realm-c";
    for (String realm : List.of(realmA, realmB, realmC)) {
      RealmRepresentation rep = new RealmRepresentation();
      rep.setRealm(realm);
      rep.setEnabled(true);
      adminClient.realms().create(rep);
    }

    // Create 12+15+16+23 = 66 orgs spread across 4 realms
    List<String> masterOrgIds = createOrgsInRealm(REALM, 12);
    List<String> realmAOrgIds = createOrgsInRealm(realmA, 15);
    List<String> realmBOrgIds = createOrgsInRealm(realmB, 16);
    List<String> realmCOrgIds = createOrgsInRealm(realmC, 23);

    List<String> allOrgIds = new ArrayList<>();
    allOrgIds.addAll(masterOrgIds);
    allOrgIds.addAll(realmAOrgIds);
    allOrgIds.addAll(realmBOrgIds);
    allOrgIds.addAll(realmCOrgIds);
    int totalOrgs = allOrgIds.size(); // 66

    // All orgs get delete-organization by default
    assertDbRoleCount(allOrgIds, ORG_ROLE_DELETE_ORGANIZATION, totalOrgs);

    // Strip the role via JDBC to simulate pre-PR#331 state across all realms
    deleteOrgRoleFromDb(allOrgIds, ORG_ROLE_DELETE_ORGANIZATION);
    assertDbRoleCount(allOrgIds, ORG_ROLE_DELETE_ORGANIZATION, 0);

    // Restart with batch size 6 — 66 orgs require ceil(66/6)=11 batches
    restartKeycloakWithBatchSize(6);

    // All 66 orgs should have the role restored, across all 4 realms
    assertDbRoleCount(allOrgIds, ORG_ROLE_DELETE_ORGANIZATION, totalOrgs);
    assertAllOrgsHaveRole(REALM, masterOrgIds, ORG_ROLE_DELETE_ORGANIZATION);
    assertAllOrgsHaveRole(realmA, realmAOrgIds, ORG_ROLE_DELETE_ORGANIZATION);
    assertAllOrgsHaveRole(realmB, realmBOrgIds, ORG_ROLE_DELETE_ORGANIZATION);
    assertAllOrgsHaveRole(realmC, realmCOrgIds, ORG_ROLE_DELETE_ORGANIZATION);

    // Second restart: idempotency — no duplicate roles should be created
    restartKeycloakWithBatchSize(6);
    assertDbRoleCount(allOrgIds, ORG_ROLE_DELETE_ORGANIZATION, totalOrgs);
  }

  private void deleteOrgRoleFromDb(List<String> orgIds, String roleName) throws SQLException {
    String placeholders = orgIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
    // user_organization_role_mapping has a FK to organization_role, so delete child rows first
    String deleteMappings =
        ("DELETE FROM user_organization_role_mapping WHERE role_id IN "
                + "(SELECT id FROM organization_role WHERE name = ? AND organization_id IN (%s))")
            .formatted(placeholders);
    String deleteRoles =
        "DELETE FROM organization_role WHERE name = ? AND organization_id IN (" + placeholders + ")";
    try (Connection conn =
        DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      try (PreparedStatement ps = conn.prepareStatement(deleteMappings)) {
        ps.setString(1, roleName);
        for (int i = 0; i < orgIds.size(); i++) {
          ps.setString(i + 2, orgIds.get(i));
        }
        ps.executeUpdate();
      }
      try (PreparedStatement ps = conn.prepareStatement(deleteRoles)) {
        ps.setString(1, roleName);
        for (int i = 0; i < orgIds.size(); i++) {
          ps.setString(i + 2, orgIds.get(i));
        }
        int deleted = ps.executeUpdate();
        assertThat("should have deleted one row per org", deleted, is(orgIds.size()));
      }
    }
  }

  private void assertDbRoleCount(List<String> orgIds, String roleName, int expectedCount)
      throws SQLException {
    String placeholders = orgIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("?");
    String sql =
        "SELECT COUNT(*) FROM organization_role WHERE name = ? AND organization_id IN ("
            + placeholders
            + ")";
    try (Connection conn =
            DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, roleName);
      for (int i = 0; i < orgIds.size(); i++) {
        ps.setString(i + 2, orgIds.get(i));
      }
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        assertThat(
            "DB count of role '" + roleName + "' for given orgs",
            rs.getInt(1),
            is(expectedCount));
      }
    }
  }
}
