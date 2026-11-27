package io.phasetwo.web;

import static io.phasetwo.service.Helpers.loadJson;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.Testcontainers;

/**
 * E2E: organization-aware login for a multi-tenant store platform.
 *
 * <p>Every store is an organization (globex, initech, acme). The app always sends the store it is
 * opening as {@code account_hint}. Rule under test: a user gets in only as a member of that store
 * or with a pending invitation to it, whatever the login method.
 *
 * <p>Everything is static, in three files under {@code src/test/resources/realms}:
 *
 * <ul>
 *   <li>{@code kc-realm-org-login-use-cases.json}: test-realm with the browser flow "shop login"
 *       (ext-inv-auto-join + ext-select-org after the session cookie and after home-IdP discovery
 *       or password), the post-broker flows "company sso post broker" (ext-auth-org-add-user,
 *       ext-inv-auto-join, ext-select-org) and "external idp post broker" (ext-inv-auto-join,
 *       ext-select-org), four identity providers, and the users. Pete is pre-linked to the
 *       partner IdP. The IdP URLs carry a {@code ${KC_PORT}} placeholder, replaced with the
 *       container's port before import.
 *   <li>{@code org-login-use-cases-upstream.json}: stands in for Globex's and Acme's company SSO,
 *       Google and a partner IdP.
 *   <li>{@code org-login-use-cases-orgs.json}: imported through {@code /orgs/import}. Stores with
 *       their email domains, the company SSO each one owns, members and pending invitations. The
 *       import marks an IdP as owned by the store but keeps the IdP's own post-broker flow, so
 *       account_hint is checked after company SSO too.
 * </ul>
 *
 * <p>The Cypress spec covers: account_hint across password, social and company SSO; a contractor
 * picking and switching stores; kc_idp_hint plus account_hint; and refusals.
 */
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressOrgLoginUseCasesTest extends AbstractCypressOrganizationTest {

  @TestFactory
  List<DynamicContainer> runOrgLoginUseCaseTests()
      throws IOException, InterruptedException, TimeoutException {

    Testcontainers.exposeHostPorts(container.getHttpPort());
    setupTestEnv();

    try (CypressContainer cypressContainer =
        new CypressContainer()
            .withBaseUrl(
                "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
            .withSpec("cypress/e2e/org-login-use-cases/org-login-use-cases.cy.ts")
            .withBrowser("electron")) {
      cypressContainer.start();
      CypressTestResults results = cypressContainer.getTestResults();
      cleanupKeycloakInstance();
      return convertToJUnitDynamicTests(results);
    }
  }

  private void setupTestEnv() throws IOException {
    importRealm(loadRealmWithPort("/realms/kc-realm-org-login-use-cases.json"), keycloak);
    importRealm("/realms/org-login-use-cases-upstream.json", null);
    importOrganizations("test-realm", "/realms/org-login-use-cases-orgs.json");
  }

  private RealmRepresentation loadRealmWithPort(String path) throws IOException {
    String json = readResource(path).replace("${KC_PORT}", String.valueOf(container.getHttpPort()));
    return loadJson(
        new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), RealmRepresentation.class);
  }

  private void importOrganizations(String realm, String path) throws IOException {
    var resp =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs/import")
            .queryParam("skipMissingMember", false)
            .queryParam("skipMissingIdp", false)
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .body(readResource(path))
            .when()
            .post()
            .then()
            .extract()
            .response();
    assertThat(
        resp.getStatusCode(),
        anyOf(
            is(Response.Status.OK.getStatusCode()),
            is(Response.Status.NO_CONTENT.getStatusCode())));
  }

  private String readResource(String path) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
