package io.phasetwo.web;

import static io.phasetwo.service.Helpers.createUserWithCredentials;

import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;

@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressSelectOrganizationTest extends AbstractCypressOrganizationTest {

  @TestFactory
  List<DynamicContainer> runCypressTests()
      throws IOException, InterruptedException, TimeoutException {

    Testcontainers.exposeHostPorts(container.getHttpPort());

    setupSelectOrgTests();

    try (CypressContainer cypressContainer =
        new CypressContainer()
            .withBaseUrl(
                "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
            .withSpec("cypress/e2e/select-organization.cy.ts")
            .withBrowser("electron")) {
      cypressContainer.start();
      CypressTestResults testResults = cypressContainer.getTestResults();
      return convertToJUnitDynamicTests(testResults);
    }
  }

  private void setupSelectOrgTests() throws IOException {
    OrganizationRepresentation org1 =
        createOrganization(
            new OrganizationRepresentation().name("org-1").domains(List.of("org1.com")));
    OrganizationRepresentation org2 =
        createOrganization(
            new OrganizationRepresentation().name("org-2").domains(List.of("org2.com")));

    // User with 2 Org
    UserRepresentation user1 = createUserWithCredentials(keycloak, REALM, "user-1", "user-1");
    grantClientRoles("account", user1.getId(), "manage-account", "view-profile");
    putRequest("pass", org1.getId(), "members", user1.getId());
    putRequest("pass", org2.getId(), "members", user1.getId());

    // User with 1 Org
    UserRepresentation user2 = createUserWithCredentials(keycloak, REALM, "user-2", "user-2");
    grantClientRoles("account", user2.getId(), "manage-account", "view-profile");
    putRequest("pass", org1.getId(), "members", user2.getId());

    // User with no Org
    UserRepresentation user3 = createUserWithCredentials(keycloak, REALM, "user-3", "user-3");
    grantClientRoles("account", user3.getId(), "manage-account", "view-profile");

    // Configure Select Org Flows
    // Define keycloak.v2 for account themes
    configureSelectOrgFlows();

    // Create a public client
    createPublicClient("public-client");
  }
}
