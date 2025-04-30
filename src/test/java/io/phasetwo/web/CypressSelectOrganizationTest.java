package io.phasetwo.web;

import static io.phasetwo.service.Helpers.createUserWithCredentials;

import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTest;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestSuite;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;

@JBossLog
@org.testcontainers.junit.jupiter.Testcontainers
class CypressSelectOrganizationTest extends AbstractCypressOrganizationTest {

  @TestFactory
  List<DynamicContainer> runCypressTests()
      throws IOException, InterruptedException, TimeoutException {
    if (!RUN_CYPRESS) {
      return Collections.emptyList();
    }

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

  private List<DynamicContainer> convertToJUnitDynamicTests(CypressTestResults testResults) {
    List<DynamicContainer> dynamicContainers = new ArrayList<>();
    List<CypressTestSuite> suites = testResults.getSuites();
    for (CypressTestSuite suite : suites) {
      createContainerFromSuite(dynamicContainers, suite);
    }
    return dynamicContainers;
  }

  private void createContainerFromSuite(
      List<DynamicContainer> dynamicContainers, CypressTestSuite suite) {
    List<DynamicTest> dynamicTests = new ArrayList<>();
    for (CypressTest test : suite.getTests()) {
      dynamicTests.add(
          DynamicTest.dynamicTest(
              test.getDescription(),
              () -> {
                if (!test.isSuccess()) {
                  log.error(test.getErrorMessage());
                  log.error(test.getStackTrace());
                }
                Assertions.assertTrue(test.isSuccess());
              }));
    }
    dynamicContainers.add(DynamicContainer.dynamicContainer(suite.getTitle(), dynamicTests));
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
