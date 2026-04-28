package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.OrganizationAdminAuth.ORG_ROLE_DELETE_ORGANIZATION;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.KeycloakOrgsAdminAPI;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import io.phasetwo.web.JbossLogConsumer;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.ToxiproxyContainer;
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

  static ToxiproxyContainer toxiproxy;

  static ToxiproxyClient toxiproxyClient;
  static Proxy postgresProxy;

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

    toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                    .withNetworkAliases("toxiproxy")
                    .withLogConsumer(new JbossLogConsumer(log))
                    .withNetwork(network);
    toxiproxy.start();

    toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getMappedPort(8474));
    postgresProxy = toxiproxyClient.createProxy("postgres", "0.0.0.0:8666", "postgres:5432");
    postgresProxy.enable();

    keycloak = buildKeycloakContainer();
    keycloak.start();

    adminClient = buildAdminClient();
  }

  @AfterAll
  static void tearDown() throws IOException {
    if (keycloak != null) stopKeycloak(keycloak);
    if (postgres != null) postgres.stop();
    if (network != null) network.close();
    if (toxiproxy != null) toxiproxy.stop();
  }

  private static boolean isJacocoPresent() {
    return Files.exists(Path.of("target/jacoco-agent/org.jacoco.agent-runtime.jar"));
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
            .withEnv("KC_DB_URL", "jdbc:postgresql://toxiproxy:8666/keycloak")
            .withEnv("KC_DB_USERNAME", "keycloak")
            .withEnv("KC_DB_PASSWORD", "keycloak");
    additionalEnvvars.forEach(kc::withEnv);
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

  private static void restartKeycloak() throws IOException {
    restartKeycloak(Map.of());
  }
  private static void restartKeycloak(Map<String, String> additionalEnvvars) throws IOException {
    stopKeycloak(keycloak);
    keycloak = buildKeycloakContainer(additionalEnvvars);
    keycloak.start();
    adminClient = buildAdminClient();
  }

  private KeycloakOrgsAdminAPI orgsApi() {
    return new KeycloakOrgsAdminAPI(keycloak.getAuthServerUrl(), REALM, adminClient);
  }

  private KeycloakOrgsAdminAPI orgsApi(String realm) {
    return new KeycloakOrgsAdminAPI(keycloak.getAuthServerUrl(), realm, buildAdminClient());
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

  // ── high-load scenario descriptors ───────────────────────────────────────

  private record LoadTestDescriptor(int realmCount,
                                    int orgsPerRealm,
                                    int roleDeletionPct) {
    String scenarioName() {
      return "realms=%d, orgs/realm=%d, roleDeleted=%d%%"
          .formatted(realmCount, orgsPerRealm, roleDeletionPct);
    }
    String realmPrefix() {
      return "hl-%d-%d-%d".formatted(realmCount, orgsPerRealm, roleDeletionPct);
    }
  }

  private record PerfResult(String scenario, int realmCount, int totalOrgs,
                             long firstRestartMs, long secondRestartMs) {}

  static final List<LoadTestDescriptor> HIGH_LOAD_SCENARIOS = List.of(
          // PreparedStatement can have at most 65,535 parameters
      new LoadTestDescriptor(5, 400, 100),   // all roles deleted
      new LoadTestDescriptor(5, 400,  95),   // 95% roles deleted
      new LoadTestDescriptor(5, 400,  50),   // half roles deleted
      new LoadTestDescriptor(5, 400, 5)      // 5% roles deleted
//          new LoadTestDescriptor(1, 30, 5)
  );

  // ── high-load test ────────────────────────────────────────────────────────

  @Test
  @Disabled("Performance benchmark — run manually only")
  void testHighLoad() throws Exception {
    List<PerfResult> results = new ArrayList<>();
    for (int i = 0; i < HIGH_LOAD_SCENARIOS.size(); i++) {
      resetInfrastructure();
      results.add(runHighLoadScenario(HIGH_LOAD_SCENARIOS.get(i)));
    }
    printPerfSummary(results);
  }

  private PerfResult runHighLoadScenario(LoadTestDescriptor d) throws Exception {
    log.infof("=== Scenario: %s ===", d.scenarioName());

    createTestRealmsAndOrgs(d.realmPrefix(), d.realmCount(), d.orgsPerRealm());

    if (d.roleDeletionPct() > 0) {
      deleteOrgRoleFromDbByPct(d.realmPrefix() + "-%", ORG_ROLE_DELETE_ORGANIZATION, d.roleDeletionPct());
      log.infof("Deleted ~%d%% of delete-organization roles.", d.roleDeletionPct());
    }

    postgresProxy.toxics().latency("upstream", ToxicDirection.UPSTREAM, 5);
    postgresProxy.toxics().latency("downstream", ToxicDirection.DOWNSTREAM, 5);
    log.info("Starting first restart.");
    long firstMs = timedRestart("Migrating missing org roles across all realms", "Organization role migration complete");
    log.infof("First restart took %d ms.", firstMs);

    log.info("Starting second restart (idempotency check).");
    long secondMs = timedRestart("Migrating missing org roles across all realms", "Organization role migration complete");
    log.infof("Second restart took %d ms.", secondMs);

    PerfResult result = new PerfResult(
        d.scenarioName(), d.realmCount(), d.realmCount() * d.orgsPerRealm(), firstMs, secondMs);
    recordPerfResult(result);
    return result;
  }

  private static void resetInfrastructure() throws IOException {
    log.info("Resetting infrastructure (dropping and recreating containers).");
    stopKeycloak(keycloak);
    postgres.stop();
    toxiproxy.stop();
    postgres = new PostgreSQLContainer<>("postgres:17")
        .withNetwork(network)
        .withNetworkAliases("postgres")
        .withDatabaseName("keycloak")
        .withUsername("keycloak")
        .withPassword("keycloak");
    postgres.start();
    toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
            .withNetworkAliases("toxiproxy")
            .withLogConsumer(new JbossLogConsumer(log))
            .withNetwork(network);
    toxiproxy.start();

    toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getMappedPort(8474));
    postgresProxy = toxiproxyClient.createProxy("postgres", "0.0.0.0:8666", "postgres:5432");
    postgresProxy.enable();
    keycloak = buildKeycloakContainer(Map.of("KC_LOG_LEVEL", "WARN"));
    keycloak.start();
    adminClient = buildAdminClient();
    log.info("Infrastructure reset complete.");
  }

  // ── high-load helpers ─────────────────────────────────────────────────────

  private void createTestRealmsAndOrgs(String realmPrefix, int realmCount, int orgsPerRealmCount) throws IOException {
    List<String> realms =
        IntStream.range(0, realmCount)
            .mapToObj(i -> "%s-%02d".formatted(realmPrefix, i))
            .toList();
    for (String realm : realms) {
      RealmRepresentation rep = new RealmRepresentation();
      rep.setRealm(realm);
      rep.setEnabled(true);
      adminClient.realms().create(rep);
    }
    log.infof("Created %d realms with prefix '%s'.", realmCount, realmPrefix);
    for (String realm : realms) {
      createOrgsInRealm(realm, orgsPerRealmCount);
      log.infof("Created %d orgs in realm %s.", orgsPerRealmCount, realm);
    }
  }

  private static final DateTimeFormatter LOG_TS_FMT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

  private long timedRestart(String startMarker, String endMarker) throws IOException {
    restartKeycloak(Map.of("KC_LOG_LEVEL", "WARN,io.phasetwo.service.resource.OrganizationResourceProviderFactory:DEBUG"));
    String[] lines = keycloak.getLogs().split("\n");
    LocalDateTime start = findLogTimestamp(lines, startMarker);
    LocalDateTime end   = findLogTimestamp(lines, endMarker);
    return Duration.between(start, end).toMillis();
  }

  private static LocalDateTime findLogTimestamp(String[] lines, String marker) {
    for (String line : lines) {
      if (line.contains(marker)) {
        return LocalDateTime.parse(line.substring(0, 23), LOG_TS_FMT);
      }
    }
    throw new IllegalStateException("Log marker not found: " + marker);
  }

  /**
   * Deletes approximately {@code pct}% of the {@code roleName} rows for every org whose
   * {@code realm_id} matches {@code realmPattern} (SQL LIKE). Uses a server-side temp table
   * so no org-ID list is sent over the wire.
   */
  private void deleteOrgRoleFromDbByPct(String realmPattern, String roleName, int pct)
      throws SQLException {
    try (Connection conn =
        DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      conn.setAutoCommit(false);
      try (PreparedStatement ps = conn.prepareStatement(
          "CREATE TEMP TABLE __roles_to_delete ON COMMIT DROP AS"
              + " SELECT r.id FROM organization_role r"
              + " JOIN organization o ON o.id = r.organization_id"
              + " WHERE r.name = ? AND o.realm_id LIKE ? AND random() < ?")) {
        ps.setString(1, roleName);
        ps.setString(2, realmPattern);
        ps.setDouble(3, pct / 100.0);
        ps.executeUpdate();
      }
      conn.createStatement().execute(
          "DELETE FROM user_organization_role_mapping"
              + " WHERE role_id IN (SELECT id FROM __roles_to_delete)");
      conn.createStatement().execute(
          "DELETE FROM organization_role WHERE id IN (SELECT id FROM __roles_to_delete)");
      conn.commit();
    }
  }

  private void recordPerfResult(PerfResult r) throws SQLException {
    try (Connection conn =
        DriverManager.getConnection(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
      conn.createStatement().execute(
          "CREATE TABLE IF NOT EXISTS migration_perf_results ("
              + "  id                SERIAL PRIMARY KEY,"
              + "  test_name         VARCHAR(200),"
              + "  realm_count       INT,"
              + "  total_org_count   INT,"
              + "  first_restart_ms  BIGINT,"
              + "  second_restart_ms BIGINT,"
              + "  recorded_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
              + ")");
      try (PreparedStatement ps = conn.prepareStatement(
          "INSERT INTO migration_perf_results"
              + " (test_name, realm_count, total_org_count, first_restart_ms, second_restart_ms)"
              + " VALUES (?, ?, ?, ?, ?)")) {
        ps.setString(1, r.scenario());
        ps.setInt(2, r.realmCount());
        ps.setInt(3, r.totalOrgs());
        ps.setLong(4, r.firstRestartMs());
        ps.setLong(5, r.secondRestartMs());
        ps.executeUpdate();
      }
    }
  }

  private void printPerfSummary(List<PerfResult> results) {
    int nameW = results.stream().mapToInt(r -> r.scenario().length()).max().orElse(10);
    String rowFmt = "%-" + nameW + "s | %6d | %9d | %12d ms | %12d ms";
    String header  = ("%-" + nameW + "s | %6s | %9s | %14s | %14s")
        .formatted("scenario", "realms", "totalOrgs", "1st restart", "2nd restart");
    String separator = "-".repeat(header.length());
    StringBuilder sb = new StringBuilder("\n=== HIGH-LOAD MIGRATION PERF SUMMARY ===\n")
        .append(header).append("\n")
        .append(separator).append("\n");
    for (PerfResult r : results) {
      sb.append(rowFmt.formatted(
              r.scenario(), r.realmCount(), r.totalOrgs(),
              r.firstRestartMs(), r.secondRestartMs()))
          .append("\n");
    }
    sb.append(separator);
    log.info(sb.toString());
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

  @Test
  void testMigrationSkipsOrphanedOrgs() throws Exception {
    // Create 3 real orgs and strip their delete-organization role so migration picks them up.
    int realOrgCount = 3;
    List<String> realOrgIds = new ArrayList<>();
    for (int i = 0; i < realOrgCount; i++) {
      OrganizationRepresentation org =
          orgsApi().createOrganization(new OrganizationRepresentation().name("orphan-test-org-" + i));
      realOrgIds.add(org.getId());
    }
    deleteOrgRoleFromDb(realOrgIds, ORG_ROLE_DELETE_ORGANIZATION);
    assertDbRoleCount(realOrgIds, ORG_ROLE_DELETE_ORGANIZATION, 0);

    // Insert an orphaned org row directly — 'ghost-realm' has no row in the REALM table.
    // This reproduces the production scenario where realm deletion left org rows behind.
    String orphanOrgId = insertOrphanedOrgDirectly("ghost-realm");

    // Sanity-check: Keycloak must start cleanly with orphaned orgs present when migration
    // is skipped — this isolates the startup problem to the migration code path, not the
    // orphaned row itself.
    restartKeycloak(Map.of("KC_ORGS_SKIP_MIGRATION", "true"));
    assertThat(
        "Keycloak should start without errors when migration is skipped",
        adminClient.realms().findAll().isEmpty(),
        is(false));

    // Now restart with migration enabled.
    // Before the fix, Collectors.toMap throws NPE when getRealm("ghost-realm") returns null,
    // aborting the batch before any role is restored.
    restartKeycloak();

    // Verify Keycloak started and the migration ran to completion.
    // Before the fix this assertion fails: the NPE aborts the migration so the completion
    // log line is never emitted (or Keycloak fails to start entirely).
    assertThat(
        "migration should complete without errors even with orphaned orgs present",
        keycloak.getLogs().contains("Organization role migration complete"),
        is(true));
    assertThat(
        "Keycloak admin API should be reachable after restart",
        adminClient.realms().findAll().isEmpty(),
        is(false));

    // Real orgs must have their role back even though an orphaned org shared the same batch.
    assertDbRoleCount(realOrgIds, ORG_ROLE_DELETE_ORGANIZATION, realOrgCount);

    // Orphaned org should remain in DB — migration skipped it, did not crash on it.
    assertOrphanedOrgStillExists(orphanOrgId);
  }

  private String insertOrphanedOrgDirectly(String ghostRealmId) throws SQLException {
    String orgId = UUID.randomUUID().toString();
    try (Connection conn =
            DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        PreparedStatement ps =
            conn.prepareStatement(
                "INSERT INTO organization (id, realm_id, name) VALUES (?, ?, ?)")) {
      ps.setString(1, orgId);
      ps.setString(2, ghostRealmId);
      ps.setString(3, "orphaned-org");
      ps.executeUpdate();
    }
    return orgId;
  }

  private void assertOrphanedOrgStillExists(String orgId) throws SQLException {
    try (Connection conn =
            DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        PreparedStatement ps =
            conn.prepareStatement("SELECT COUNT(*) FROM organization WHERE id = ?")) {
      ps.setString(1, orgId);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        assertThat("orphaned org should remain in DB after migration", rs.getInt(1), is(1));
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
