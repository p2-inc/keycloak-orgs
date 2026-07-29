package io.phasetwo.service.importexport;

import static io.phasetwo.service.AbstractOrganizationTest.KEYCLOAK_IMAGE;
import static io.phasetwo.service.AbstractOrganizationTest.getDeps;
import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.KeycloakOrgsAdminAPI;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Reproduces the NPE reported from production logs:
 *
 * <pre>
 * java.lang.NullPointerException: Cannot invoke "org.keycloak.models.UserModel.getServiceAccountClientLink()"
 * because "u" is null
 *   at io.phasetwo.service.model.jpa.OrganizationAdapter.lambda$getMembersStream$1(OrganizationAdapter.java:335)
 * </pre>
 *
 * <p>{@code ORGANIZATION_MEMBER.USER_ID} has no foreign key to the Keycloak user table, so a
 * membership row can outlive the user it references (e.g. the user was deleted through a path
 * that bypassed the {@code UserModel.UserRemovedEvent} listener). {@code
 * OrganizationAdapter#getMembersStream} looks the user up by that id and, unlike its sibling
 * {@code searchForMembersStream}, does not filter out a null result before dereferencing it. This
 * test simulates the orphaned row directly via JDBC (no FK stops it) and drives the one
 * REST-reachable caller of the unfiltered method: {@code GET .../orgs/export?exportMembersAndInvitations=true}.
 */
public class OrganizationMemberOrphanedUserExportTest {

  private static final Network network = Network.newNetwork();

  private static final PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16")
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withDatabaseName("keycloak")
          .withUsername("keycloak")
          .withPassword("password");

  private static final KeycloakContainer keycloakContainer =
      new KeycloakContainer(KEYCLOAK_IMAGE)
          .withNetwork(network)
          .withContextPath("/auth")
          .withProviderClassesFrom("target/classes")
          .withProviderLibsFrom(getDeps())
          .withEnv("KC_DB", "postgres")
          .withEnv("KC_DB_URL", "jdbc:postgresql://postgres:5432/keycloak?loggerLevel=OFF")
          .withEnv("KC_DB_USERNAME", postgres.getUsername())
          .withEnv("KC_DB_PASSWORD", postgres.getPassword())
          .withAccessToHost(true);

  private static final String REALM = "master";

  private static Keycloak keycloak;

  @BeforeAll
  public static void setUp() {
    postgres.start();
    keycloakContainer.start();

    keycloak =
        Keycloak.getInstance(
            keycloakContainer.getAuthServerUrl(),
            REALM,
            keycloakContainer.getAdminUsername(),
            keycloakContainer.getAdminPassword(),
            "admin-cli");
  }

  @AfterAll
  public static void tearDown() {
    if (keycloakContainer != null) {
      keycloakContainer.stop();
    }
    if (postgres != null) {
      postgres.stop();
    }
    if (network != null) {
      network.close();
    }
  }

  private KeycloakOrgsAdminAPI orgsApi() {
    return new KeycloakOrgsAdminAPI(keycloakContainer.getAuthServerUrl(), REALM, keycloak);
  }

  @Test
  @DisplayName(
      "Export with an orphaned ORGANIZATION_MEMBER row (user deleted outside the event listener) "
          + "should not NPE, and should skip the orphaned member")
  void exportSkipsOrphanedMembershipInsteadOfThrowing() throws Exception {
    // one real member, added through the normal REST flow
    UserRepresentation member = createUser(keycloak, REALM, "orgmember-" + UUID.randomUUID());

    OrganizationRepresentation org =
        orgsApi().createOrganization(new OrganizationRepresentation().name("orphan-member-org"));

    addMember(org.getId(), member.getId());

    // simulate a membership row whose user no longer exists. USER_ID has no FK to the Keycloak
    // user table, so this state is reachable in production (e.g. a user removed by a path that
    // skips the UserRemovedEvent listener in OrganizationResourceProviderFactory), not just a
    // theoretical DB state.
    insertOrphanedMembership(org.getId());

    Response exportResponse =
        given()
            .baseUri(keycloakContainer.getAuthServerUrl())
            .basePath("realms/" + REALM + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .queryParam("exportMembersAndInvitations", true)
            .when()
            .get("export")
            .andReturn();

    // Before the fix: getMembersStream() NPEs on the orphaned row and the whole export 500s.
    assertThat(
        "export should succeed instead of 500ing on the orphaned membership row",
        exportResponse.getStatusCode(),
        is(Status.OK.getStatusCode()));

    KeycloakOrgsRepresentation export =
        objectMapper()
            .readValue(exportResponse.getBody().asString(), KeycloakOrgsRepresentation.class);

    var exportedOrg =
        export.getOrganizations().stream()
            .filter(o -> o.getOrganization().getName().equals(org.getName()))
            .findFirst()
            .orElseThrow();

    // the orphaned row must be skipped, not surfaced as a phantom member
    assertThat(exportedOrg.getMembers(), hasSize(1));
    assertThat(
        exportedOrg.getMembers().stream().map(m -> m.getUsername()).toList(),
        contains(member.getUsername()));
  }

  private void addMember(String orgId, String userId) {
    Response response =
        given()
            .baseUri(keycloakContainer.getAuthServerUrl())
            .basePath("realms/" + REALM + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .when()
            .put(orgId + "/members/" + userId)
            .andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
  }

  private void insertOrphanedMembership(String orgId) throws SQLException {
    try (Connection conn =
            DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        PreparedStatement ps =
            conn.prepareStatement(
                "INSERT INTO organization_member (id, created_at, user_id, organization_id) "
                    + "VALUES (?, now(), ?, ?)")) {
      ps.setString(1, UUID.randomUUID().toString());
      ps.setString(2, UUID.randomUUID().toString()); // no matching USER_ENTITY row, by design
      ps.setString(3, orgId);
      ps.executeUpdate();
    }
  }
}
