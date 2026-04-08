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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.testcontainers.Testcontainers;

import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressSelectOrganizationTest extends AbstractCypressOrganizationTest {

  private List<String> knownRealms;

    @BeforeEach
    public void setup() {
        knownRealms = new ArrayList<>();
    }

    @AfterEach
    public void cleanupKeycloakInstance() {
        List.copyOf(knownRealms)
                .forEach(realmName -> {
                    findRealmByName(realmName).remove();
                    knownRealms.remove(realmName);
                });
    }

  @TestFactory
  List<DynamicContainer> runCypressTests()
      throws IOException, InterruptedException, TimeoutException {

    Testcontainers.exposeHostPorts(container.getHttpPort());

      // import client realm
    importRealm();
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
    var realm = findRealmByName("test-realm");
    OrganizationRepresentation org1 =
        createOrganization("test-realm",
            new OrganizationRepresentation().name("org-1").domains(List.of("org1.com")));
    OrganizationRepresentation org2 =
        createOrganization("test-realm",
            new OrganizationRepresentation().name("org-2").domains(List.of("org2.com")));
    var user1 = keycloak.realm(knownRealms.getFirst()).users().search("user1", true).getFirst();
    createMembership("test-realm", org1.getId(), "members", user1.getId());
    createMembership("test-realm", org2.getId(), "members", user1.getId());

    var user2 = keycloak.realm(knownRealms.getFirst()).users().search("user2", true).getFirst();
    createMembership("test-realm", org1.getId(), "members", user2.getId());

    createClient();
  }

    private static void createClient() {
        ClientRepresentation client = new ClientRepresentation();

        client.setProtocol("openid-connect");
        client.setClientId("public-client");
        client.setName("public-client");
        client.setDescription("");
        client.setPublicClient(true);
        client.setAuthorizationServicesEnabled(false);
        client.setServiceAccountsEnabled(false);
        client.setImplicitFlowEnabled(false);
        client.setDirectAccessGrantsEnabled(true);
        client.setStandardFlowEnabled(true);
        client.setFrontchannelLogout(true);
        client.setAlwaysDisplayInConsole(false);
        client.setRootUrl("http://localhost:3000");
        client.setBaseUrl("http://localhost:3000");

        // Lists/Arrays
        client.setRedirectUris(Collections.singletonList("*"));
        client.setWebOrigins(Collections.singletonList("*"));

        // Attributes
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put("saml_idp_initiated_sso_url_name", "");
        attrMap.put("oauth2.device.authorization.grant.enabled", "false"); // Note: Attributes are String-based
        attrMap.put("oidc.ciba.grant.enabled", "false");
        client.setAttributes(attrMap);
        // Create a public client
        keycloak.realm("test-realm").clients()
                .create(client);
    }

    private void importRealm() {
      var realmRepresentation = importRealm("/realms/kc-realm-org-browser-flow.json", null);
      knownRealms.add(realmRepresentation.getRealm());
  }

    // create an organization, fet the created organization and returns it
    protected OrganizationRepresentation createOrganization(String realm,OrganizationRepresentation representation) throws IOException {
        var response = given()
                .baseUri(container.getAuthServerUrl())
                .basePath("realms/" + realm + "/" + OrganizationResourceProviderFactory.ID)
                .contentType("application/json")
                .auth()
                .oauth2(keycloak.tokenManager().getAccessTokenString())
                .and()
                .body(toJsonString(representation))
                .when()
                .post()
                .then()
                .extract()
                .response();
        assertThat(response.getStatusCode(), Matchers.is(Response.Status.CREATED.getStatusCode()));
        assertNotNull(response.getHeader("Location"));
        String loc = response.getHeader("Location");
        String id = loc.substring(loc.lastIndexOf("/") + 1);

        response = getRequest(id);
        assertThat(response.statusCode(), Matchers.is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
        OrganizationRepresentation orgRep =
                objectMapper().readValue(response.getBody().asString(), OrganizationRepresentation.class);
        assertThat(orgRep.getId(), is(id));
        return orgRep;
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
