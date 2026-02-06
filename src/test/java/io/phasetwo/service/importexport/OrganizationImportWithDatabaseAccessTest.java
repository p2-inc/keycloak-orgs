package io.phasetwo.service.importexport;

import static io.phasetwo.service.AbstractOrganizationTest.KEYCLOAK_IMAGE;
import static io.phasetwo.service.AbstractOrganizationTest.getDeps;
import static io.phasetwo.service.Helpers.loadJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Test class for verifying the functionality of the organization import endpoints in a Keycloak
 * environment, along with database query validations. This test class uses a PostgreSQL container
 * and Keycloak container as part of its test setup.
 *
 *  The tests primarily focus on:
 * - Importing a realm into Keycloak.
 * - Importing organizations into the realm.
 * - Validating the database to ensure the imported organizations are correctly added.
 *
 * With these validations, the test ensures that we store what we expect in the database, and there's no hidden
 * error in the API-level conversions
 *
 */
@DisplayName("Tests for the Organization import endpoints with database-query verifications")
public class OrganizationImportWithDatabaseAccessTest {

    private static Network network = Network.newNetwork();

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withNetwork(network)
                    .withNetworkAliases("postgres")
                    .withDatabaseName("keycloak")
                    .withUsername("keycloak")
                    .withPassword("password");

    private static KeycloakContainer keycloakContainer =
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
    private static Keycloak keycloak;

    @BeforeAll
    public static void setUp() {
        postgres.start();
        keycloakContainer.start();

        keycloak =
                Keycloak.getInstance(
                        keycloakContainer.getAuthServerUrl(),
                        "master",
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

    @Test
    @DisplayName("Import organization with empty string as domain and verify that the empty domain is not stored in the database")
    void testOrganizationWithEmptyDomainImportAndVerifyInDatabase() throws SQLException {
        String realmName = "test-realm";

        // 1. Import Realm
        RealmRepresentation testRealm =
                loadJson(
                        OrganizationImportWithDatabaseAccessTest.class.getResourceAsStream(
                                "/orgs/keycloak-realm-with-identity-provider.json"),
                        RealmRepresentation.class);
        testRealm.setRealm(realmName);

        var realmResponse =
                given()
                        .baseUri(keycloakContainer.getAuthServerUrl())
                        .basePath("admin/realms/")
                        .contentType("application/json")
                        .auth()
                        .oauth2(keycloak.tokenManager().getAccessTokenString())
                        .and()
                        .body(testRealm)
                        .when()
                        .post()
                        .then()
                        .extract()
                        .response();
        assertThat(realmResponse.getStatusCode(), CoreMatchers.is(Response.Status.CREATED.getStatusCode()));

        // 2. Import Organization
        KeycloakOrgsRepresentation orgsRepresentation =
                loadJson(
                        OrganizationImportWithDatabaseAccessTest.class.getResourceAsStream(
                                "/orgs/org-import-test.json"),
                        KeycloakOrgsRepresentation.class);

        var orgsResponse =
                given()
                        .baseUri(keycloakContainer.getAuthServerUrl())
                        .basePath("realms/" + realmName + "/orgs")
                        .contentType("application/json")
                        .auth()
                        .oauth2(keycloak.tokenManager().getAccessTokenString())
                        .queryParam("skipMissingMember", false)
                        .queryParam("skipMissingIdp", false)
                        .when()
                        .and()
                        .body(orgsRepresentation)
                        .when()
                        .post("import")
                        .then()
                        .extract()
                        .response();
        assertThat(orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.OK.getStatusCode()));

        // 3. Verify in Database
        try (Connection conn =
                     DriverManager.getConnection(
                             postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM ORGANIZATION WHERE NAME = 'test1'");
            rs.next();
            assertThat("Organization test1 should exist in database", rs.getInt(1), is(1));

            rs = stmt.executeQuery("SELECT count(*) FROM ORGANIZATION_DOMAIN WHERE domain = ''");
            rs.next();
            assertThat("There should be no empty string domains in the database", rs.getInt(1), is(0));
        }
    }
}
