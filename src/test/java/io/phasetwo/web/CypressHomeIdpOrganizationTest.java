package io.phasetwo.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.representation.LinkIdp;
import jakarta.ws.rs.core.Response;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.Testcontainers;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static io.phasetwo.service.Helpers.*;
import static io.phasetwo.service.Orgs.ORG_CONFIG_MULTIPLE_IDPS_KEY;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressHomeIdpOrganizationTest extends AbstractCypressOrganizationTest {

    private static final String OIDC_IDP = "oidc-idp";

    @TestFactory
    Stream<DynamicContainer> baseHomeIdpTests() {
        return Stream
                .of(false, null)
                .map(isIdpLinkOnlyValue -> {
                            try {
                                setupTestKeycloakInstance(isIdpLinkOnlyValue);
                                List<DynamicContainer> dynamicContainers = runCypressTests("IDP's isLinkOnly is: %s - ".formatted(isIdpLinkOnlyValue), "cypress/e2e/home-idp-organization/base.cy.ts");
                                cleanupKeycloakInstance();
                                return dynamicContainers;
                            } catch (IOException | InterruptedException | TimeoutException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .flatMap(Collection::stream);
    }

    @TestFactory
    public List<DynamicContainer> bypassLoginEnabledTests() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false);
        final var realm = keycloak
                .realms()
                .realm("test-realm");

        final var homeIdpDiscoveryExecution = realm
                .flows()
                .getExecutions("Copy of browser forms")
                .stream()
                .filter(execution -> Objects.equals(execution.getProviderId(), "ext-auth-home-idp-discovery"))
                .findFirst()
                .orElseThrow();
        final var homeIdpDiscoveryConfig = realm.flows().getAuthenticatorConfig(homeIdpDiscoveryExecution.getAuthenticationConfig());
        homeIdpDiscoveryConfig.getConfig().put("bypassLoginPage", "true");
        realm.flows().updateAuthenticatorConfig(homeIdpDiscoveryConfig.getId(), homeIdpDiscoveryConfig);
        List<DynamicContainer> dynamicContainers = runCypressTests(
                "",
                "cypress/e2e/home-idp-organization/bypass-login-enabled.cy.ts"
        );
        cleanupKeycloakInstance();
        return dynamicContainers;
    }

    @TestFactory
    @DisplayName("In the Organization settings the 'Multiple IDPs enabled' turned on")
    public Stream<DynamicContainer> multipleIdpsEnabledForOgranizationsWithForwardToFirstOffTests() {
        enum ForwardToFirstMatch {
            TRUE("MutlipleIDPs when forwardToFirstMatch ", "multiple-idps-enabled-with-forwarding.cy.ts"),
            FALSE("MutlipleIDPs when don't forwardToFirstMatch ", "multiple-idps-enabled-without-forwarding.cy.ts");

            private final String testPrefix;
            final String testFile;

            ForwardToFirstMatch(String testPrefix, String testFile) {
                this.testPrefix = testPrefix;
                this.testFile = testFile;
            }
        }

        return Stream.of(ForwardToFirstMatch.values()).map(forwardToFirstMatch -> {
            try {
                setupTestKeycloakInstance(false);
                if (forwardToFirstMatch.equals(ForwardToFirstMatch.FALSE)) {
                    modifyAuthenticatorConfig(
                            "test-realm",
                            "Copy of browser forms",
                            "ext-auth-home-idp-discovery",
                            Map.entry("forwardToFirstMatch", "false")
                    );
                }

                final var realm = keycloak
                        .realms()
                        .realm("test-realm");

                RealmRepresentation realmRepresentation = realm.toRepresentation();
                Map<String, String> attributes = new HashMap<>(realmRepresentation.getAttributesOrEmpty());
                realmRepresentation.setAttributes(attributes);
                attributes.put(ORG_CONFIG_MULTIPLE_IDPS_KEY, "true");
                realm.update(realmRepresentation);

                createIdentityProviderIn(realm, "second-oidc", false);
                final var organization = findOrganizationRepresentationByName(realm.toRepresentation(), "org-home");
                linkIdentityProviderToOrganization(realm.toRepresentation(), organization, "second-oidc");

                List<DynamicContainer> dynamicContainers = runCypressTests(
                        forwardToFirstMatch.testPrefix,
                        "cypress/e2e/home-idp-organization/" + forwardToFirstMatch.testFile
                );
                cleanupKeycloakInstance();
                return dynamicContainers;
            } catch (IOException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }).flatMap(Collection::stream);
    }

    @TestFactory
    @DisplayName("The UsernameNoteAuthenticator should ask the username, handle extra steps, and continue with HomeIDP Authentication")
    public List<DynamicContainer> testUsernameInAuthNoteFormWithHomeIdpIfTheresExtraAuthenticationFormPartsBetween() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false, "/realms/kc-realm-with-home-idp-with-username-auth-note-flow.json");
        final var cypressResult = runCypressTests(
                "",
                "cypress/e2e/home-idp-organization/auth-note-form-with-home-idp.cy.ts");
        cleanupKeycloakInstance();
        return cypressResult;
    }

    private void modifyAuthenticatorConfig(String realmName, String flowAlias, String providerId, Map.Entry<String, String> modifiedConfig) {
        final var realm = keycloak
                .realms()
                .realm(realmName);

        final var homeIdpDiscoveryExecution = realm
                .flows()
                .getExecutions(flowAlias)
                .stream()
                .filter(execution -> Objects.equals(execution.getProviderId(), providerId))
                .findFirst()
                .orElseThrow();
        final var homeIdpDiscoveryConfig = realm.flows().getAuthenticatorConfig(homeIdpDiscoveryExecution.getAuthenticationConfig());
        homeIdpDiscoveryConfig.getConfig().put(modifiedConfig.getKey(), modifiedConfig.getValue());
        realm.flows().updateAuthenticatorConfig(homeIdpDiscoveryConfig.getId(), homeIdpDiscoveryConfig);
    }

    private void setupTestKeycloakInstance(Boolean isIdpLinkOnlyValue) throws JsonProcessingException {
        setupTestKeycloakInstance(isIdpLinkOnlyValue, "/realms/kc-realm-with-home-idp-flow.json");
    }

    private void setupTestKeycloakInstance(Boolean isIdpLinkOnlyValue, String testRealmRepresentationLocation) throws JsonProcessingException {
        Testcontainers.exposeHostPorts(container.getHttpPort());

        // import testRealm
        RealmRepresentation testRealm =
                loadJson(getClass().getResourceAsStream(testRealmRepresentationLocation),
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
        createIdentityProviderIn(keycloak.realm("test-realm"), OIDC_IDP, isIdpLinkOnlyValue);

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

        OrganizationRepresentation orgRep = findOrganizationRepresentationById(testRealm, id);

        // link it
        linkIdentityProviderToOrganization(testRealm, orgRep, OIDC_IDP);
    }

    private @NotNull OrganizationRepresentation findOrganizationRepresentationById(RealmRepresentation testRealm, String id) throws JsonProcessingException {
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
        return orgRep;
    }

    private @NotNull OrganizationRepresentation findOrganizationRepresentationByName(RealmRepresentation testRealm, String name) throws JsonProcessingException {
        // get organization
        var response =
                given()
                        .baseUri(container.getAuthServerUrl())
                        .basePath("realms/" + testRealm.getRealm() + "/orgs/")
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
        List<OrganizationRepresentation> orgReps =
                objectMapper().readValue(response.getBody().asString(), new TypeReference<List<OrganizationRepresentation>>() {
                });
        final var orgRep = orgReps.stream().filter(org -> org.getName().equals(name)).findFirst().orElseThrow();
        assertThat(orgRep.getName(), CoreMatchers.is(name));
        return orgRep;
    }

    private void linkIdentityProviderToOrganization(
            RealmRepresentation testRealm,
            OrganizationRepresentation orgRep,
            String identityProviderAlias) throws JsonProcessingException {
        LinkIdp link = new LinkIdp();
        link.setAlias(identityProviderAlias);
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
        assertThat(response2.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));
    }

    private void createIdentityProviderIn(RealmResource realm, String providerAlias, Boolean isIdpLinkOnlyValue) {
        org.keycloak.representations.idm.IdentityProviderRepresentation idp =
                new org.keycloak.representations.idm.IdentityProviderRepresentation();
        idp.setAlias(providerAlias);
        idp.setProviderId("oidc");
        idp.setEnabled(true);
        idp.setFirstBrokerLoginFlowAlias("first broker login");
        idp.setLinkOnly(isIdpLinkOnlyValue);
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
        realm.identityProviders().create(idp);
    }

    private void cleanupKeycloakInstance() {
        keycloak.realms().realm("test-realm").remove();
        keycloak.realms().realm("external-idp").remove();
    }

    private @NotNull List<DynamicContainer> runCypressTests(String testContainerNamePrefix, String cypressTestFile) throws InterruptedException, TimeoutException, IOException {
        List<DynamicContainer> dynamicContainers = new ArrayList<>();
        try (CypressContainer cypressContainer =
                     new CypressContainer()
                             .withBaseUrl(
                                     "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
                             .withSpec(cypressTestFile)
                             .withBrowser("electron")) {
            cypressContainer.start();
            CypressTestResults testResults = cypressContainer.getTestResults();
            dynamicContainers.addAll(convertToJUnitDynamicTests(testContainerNamePrefix, testResults));
        }
        return dynamicContainers;
    }
}
