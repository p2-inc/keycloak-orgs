package io.phasetwo.web;

import com.google.common.collect.ImmutableMap;
import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTest;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestSuite;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.representation.LinkIdp;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.Testcontainers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static io.phasetwo.service.Helpers.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JBossLog
@org.testcontainers.junit.jupiter.Testcontainers
class CypressHomeIdpOrganizationTest extends AbstractCypressOrganizationTest {

    private static final String OIDC_IDP = "oidc-idp";

    @TestFactory
    List<DynamicContainer> runCypressTests()
            throws IOException, InterruptedException, TimeoutException {
    if (!RUN_CYPRESS) {
      return Collections.emptyList();
    }

        Testcontainers.exposeHostPorts(container.getHttpPort());

        // import testRealm
        RealmRepresentation testRealm =
                loadJson(getClass().getResourceAsStream("/realms/kc-realm-with-home-idp-flow.json"),
                        RealmRepresentation.class);
        importRealm(testRealm, keycloak);

        // import client realm
        RealmRepresentation clientRealm =
                loadJson(getClass().getResourceAsStream("/realms/external-idp.json"),
                        RealmRepresentation.class);
        importRealm(clientRealm, keycloak);

        //update redirectUris
        var redirectUri = "http://host.testcontainers.internal:%s/auth/realms/test-realm/broker/oidc-idp/endpoint".formatted(container.getHttpPort());
        var clientRep = keycloak.realm(clientRealm.getRealm()).clients().findByClientId("test-realm-client").get(0);
        clientRep.setRedirectUris(List.of(redirectUri));
        keycloak.realm(clientRealm.getRealm()).clients().get(clientRep.getId()).update(clientRep);

        //create idp
        org.keycloak.representations.idm.IdentityProviderRepresentation idp =
                new org.keycloak.representations.idm.IdentityProviderRepresentation();
        idp.setAlias(OIDC_IDP);
        idp.setProviderId("oidc");
        idp.setEnabled(true);
        idp.setFirstBrokerLoginFlowAlias("first broker login");
        idp.setConfig(
                new ImmutableMap.Builder<String, String>()
                        .put("useJwksUrl", "true")
                        .put("syncMode", "FORCE")
                        .put("authorizationUrl", "http://host.testcontainers.internal:%s/auth/realms/external-idp/protocol/openid-connect/auth".formatted(container.getHttpPort()))
                        .put("hideOnLoginPage", "")
                        .put("loginHint", "")
                        .put("uiLocales", "")
                        .put("backchannelSupported", "")
                        .put("disableUserInfo", "")
                        .put("acceptsPromptNoneForwardFromClient", "")
                        .put("validateSignature", "")
                        .put("pkceEnabled", "")
                        .put("tokenUrl", "http://host.testcontainers.internal:%s/auth/realms/external-idp/protocol/openid-connect/token".formatted(container.getHttpPort()))
                        .put("clientAuthMethod", "client_secret_post")
                        .put("clientId", "test-realm-client")
                        .put("clientSecret", "secret-123")
                        .build());
        keycloak.realm("test-realm").identityProviders().create(idp);

        //create organization with domain
        var representation = new OrganizationRepresentation().name("org-home").domains(List.of("phasetwo.io"));
        var createOrgResponse =
                given()
                        .baseUri(container.getAuthServerUrl())
                        .basePath("realms/" + testRealm.getRealm() + "/orgs")
                        .contentType("application/json")
                        .auth()
                        .oauth2(keycloak.tokenManager().getAccessTokenString())
                        .body(toJsonString(representation))
                        .when()
                        .post()
                        .andReturn();

        assertThat(createOrgResponse.getStatusCode(), CoreMatchers.is(Response.Status.CREATED.getStatusCode()));
        assertNotNull(createOrgResponse.getHeader("Location"));
        String loc = createOrgResponse.getHeader("Location");
        String id = loc.substring(loc.lastIndexOf("/") + 1);

        // get organization
        var response =
                given()
                        .baseUri(container.getAuthServerUrl())
                        .basePath("realms/" + testRealm.getRealm() + "/orgs/" + id)
                        .contentType("application/json")
                        .auth()
                        .oauth2(keycloak.tokenManager().getAccessTokenString())
                        .and()
                        .when()
                        .get()
                        .then()
                        .extract()
                        .response();
       assertThat(response.statusCode(), Matchers.is(Response.Status.OK.getStatusCode()));
       OrganizationRepresentation orgRep =
                objectMapper().readValue(response.getBody().asString(), OrganizationRepresentation.class);
       assertThat(orgRep.getId(), CoreMatchers.is(id));

        // link it
        LinkIdp link = new LinkIdp();
        link.setAlias(OIDC_IDP);
        link.setSyncMode("IMPORT");
        var response2 =
                given()
                        .baseUri(container.getAuthServerUrl())
                        .basePath("realms/" + testRealm.getRealm() + "/orgs/" + orgRep.getId() + "/idps/link")
                        .contentType("application/json")
                        .auth()
                        .oauth2(keycloak.tokenManager().getAccessTokenString())
                        .and()
                        .body(toJsonString(link))
                        .when()
                        .post()
                        .then()
                        .extract()
                        .response();
        assertThat(response2.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));


        List<DynamicContainer> dynamicContainers = new ArrayList<>();
        try (CypressContainer cypressContainer =
                     new CypressContainer()
                             .withBaseUrl(
                                     "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
                             .withSpec("cypress/e2e/home-idp-organization.cy.ts")
                             .withBrowser("electron")) {
            cypressContainer.start();
            CypressTestResults testResults = cypressContainer.getTestResults();
            dynamicContainers.addAll(convertToJUnitDynamicTests(testResults));
        }

        keycloak.realms().realm("test-realm").remove();
        keycloak.realms().realm("external-idp").remove();
        return dynamicContainers;
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
}
