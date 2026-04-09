package io.phasetwo.web;

import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import io.phasetwo.service.resource.OrganizationResourceProviderFactory;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.Testcontainers;

import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressSelectOrganizationTest extends AbstractCypressOrganizationTest {

  @TestFactory
  List<DynamicContainer> runCypressTests()
      throws IOException, InterruptedException, TimeoutException {

    Testcontainers.exposeHostPorts(container.getHttpPort());

      // import client realm
    importRealm("/realms/kc-realm-org-browser-flow.json", null);
    setupSelectOrgTests();

    try (CypressContainer cypressContainer =
        new CypressContainer()
            .withBaseUrl(
                "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
            .withSpec("cypress/e2e/select-organization.cy.ts")
            .withBrowser("electron")) {
      cypressContainer.start();
      CypressTestResults testResults = cypressContainer.getTestResults();
      cleanupKeycloakInstance();
      return convertToJUnitDynamicTests(testResults);
    }
  }

  private void setupSelectOrgTests() throws IOException {
    final var realm = findRealmByName(getKnownRealms().getFirst());
    final var realmRep = realm.toRepresentation();
    OrganizationRepresentation org1 =
        createOrganization(realmRep,
            new OrganizationRepresentation().name("org-1").domains(List.of("org1.com")));
    OrganizationRepresentation org2 =
        createOrganization(realmRep,
            new OrganizationRepresentation().name("org-2").domains(List.of("org2.com")));
    var user1 = realm.users().search("user-1", true).getFirst();
    createMembership(realmRep.getRealm(), org1.getId(), "members", user1.getId());
    createMembership(realmRep.getRealm(), org2.getId(), "members", user1.getId());

    var user2 = realm.users().search("user-2", true).getFirst();
    createMembership(realmRep.getRealm(), org1.getId(), "members", user2.getId());
  }

    protected void createMembership(String realm, String... paths) throws IOException {
        var response = given()
                .baseUri(container.getAuthServerUrl())
                .basePath("realms/" + realm + "/" + OrganizationResourceProviderFactory.ID)
                .contentType("application/json")
                .auth()
                .oauth2(keycloak.tokenManager().getAccessTokenString())
                .and()
                .body(toJsonString("foo"))
                .when()
                .put(String.join("/", paths))
                .then()
                .extract()
                .response();
        assertThat(response.getStatusCode(), Matchers.is(Response.Status.CREATED.getStatusCode()));
    }
}
