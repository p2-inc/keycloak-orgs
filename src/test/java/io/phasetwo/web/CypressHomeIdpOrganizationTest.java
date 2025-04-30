package io.phasetwo.web;

import com.google.common.collect.ImmutableMap;
import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTest;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestSuite;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.representation.LinkIdp;
import io.restassured.response.Response;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.loadJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@JBossLog
@org.testcontainers.junit.jupiter.Testcontainers
class CypressHomeIdpOrganizationTest extends AbstractCypressOrganizationTest {

    @TestFactory
    List<DynamicContainer> runCypressTests()
            throws IOException, InterruptedException, TimeoutException {
    if (!RUN_CYPRESS) {
      return Collections.emptyList();
    }

        Testcontainers.exposeHostPorts(container.getHttpPort());

        // import realm
        RealmRepresentation testRealm =
                loadJson(getClass().getResourceAsStream("/realms/kc-realm-with-home-idp-flow.json"),
                        RealmRepresentation.class);
        importRealm(testRealm, keycloak);

        //create organization with domain
        OrganizationRepresentation org1 =
                createOrganization(
                        new OrganizationRepresentation().name("org-home").domains(List.of("phasetwo.io")));

        // add user in  Org1
        UserRepresentation user1 = createUserWithCredentials(keycloak, testRealm.getRealm(), "user-1", "user-1");
        putRequest("pass", org1.getId(), "members", user1.getId());

        //create a idp link
        String alias1 = "linking-provider-1";
        org.keycloak.representations.idm.IdentityProviderRepresentation idp =
                new org.keycloak.representations.idm.IdentityProviderRepresentation();
        idp.setAlias(alias1);
        idp.setProviderId("oidc");
        idp.setEnabled(true);
        idp.setFirstBrokerLoginFlowAlias("first broker login");
        idp.setConfig(
                new ImmutableMap.Builder<String, String>()
                        .put("useJwksUrl", "true")
                        .put("syncMode", "FORCE")
                        .put("authorizationUrl", "https://marketplace.stripe.com/oauth/v2/authorize")
                        .put("hideOnLoginPage", "")
                        .put("loginHint", "")
                        .put("uiLocales", "")
                        .put("backchannelSupported", "")
                        .put("disableUserInfo", "")
                        .put("acceptsPromptNoneForwardFromClient", "")
                        .put("validateSignature", "")
                        .put("pkceEnabled", "")
                        .put("tokenUrl", "https://api.stripe.com/v1/oauth/token")
                        .put("clientAuthMethod", "client_secret_post")
                        .put("clientId", "aabbcc")
                        .put("clientSecret", "112233")
                        .build());
        keycloak.realm(REALM).identityProviders().create(idp);

        // link it
        LinkIdp link = new LinkIdp();
        link.setAlias(alias1);
        link.setSyncMode("IMPORT");
        Response response = postRequest(link, org1.getId(), "idps", "link");
        assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

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
