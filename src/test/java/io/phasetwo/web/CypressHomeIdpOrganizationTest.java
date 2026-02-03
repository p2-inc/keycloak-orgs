package io.phasetwo.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.util.IdentityProviders;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static io.phasetwo.service.Helpers.*;
import static io.phasetwo.service.Orgs.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JBossLog
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressHomeIdpOrganizationTest extends AbstractCypressOrganizationTest {

    private static final String OIDC_IDP = "oidc-idp";
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
    @DisplayName("Base testcases")
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
    @DisplayName("Should not redirect the user when the IDP is disabled for the org")
    public List<DynamicContainer> redirectorWithDisabledIdp() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false);
        final var realm = findRealmByName("test-realm");
        final var idp = realm.identityProviders().get("oidc-idp").toRepresentation();
        idp.setEnabled(false);
        realm.identityProviders().get(idp.getAlias()).update(idp);
        List<DynamicContainer> dynamicContainers = runCypressTests("", "cypress/e2e/home-idp-organization/base-with-disabled-org-idp.cy.ts");
        return dynamicContainers;
    }

    @TestFactory
    @DisplayName("Test the IDP Validation attributes when pending IDPs could be present")
    public Stream<DynamicContainer> idpValidationEnabledWithUnvalidatedIdp() throws IOException, InterruptedException, TimeoutException {
        enum ThirdIdentityProviderValidated {
            FALSE("There is an unvalidated IDP ", "cypress/e2e/home-idp-organization/base-with-idp-validation-enabled-and-unvalidated-idp.cy.ts"),
            TRUE("All the IDPs are validated ", "cypress/e2e/home-idp-organization/base-with-idp-validation-enabled-and-validated-idp.cy.ts");

            private final String testPrefix;
            final String testFile;

            ThirdIdentityProviderValidated(String testPrefix, String testFile) {
                this.testPrefix = testPrefix;
                this.testFile = testFile;
            }
        }

        return Stream
                .of(ThirdIdentityProviderValidated.values())
                .map(thirdIdentityProviderValidated -> {
                    try {
                        setupKeycloakInstanceWithMultipleIdpsEnabledAtOrganization(); // sets up the keycloak with ~2 IDP configs
                        final var realm = findRealmByName("test-realm");
                        final var thirdClientRealm = importRealm("/realms/external-idp.json", "third-external-idp");

                        final var thirdIdentityProvider = createIdentityProviderIn(realm, "third-oidc", false, "third-external-idp");
                        String isIdpPending = Boolean.valueOf(!Boolean.parseBoolean(thirdIdentityProviderValidated.name().toLowerCase())).toString();
                        thirdIdentityProvider.getConfig().put(ORG_VALIDATION_PENDING_CONFIG_KEY, isIdpPending);
                        updateRedirectUriInClientRealm(thirdClientRealm, thirdIdentityProvider);
                        realm.identityProviders().get(thirdIdentityProvider.getAlias()).update(thirdIdentityProvider);

                        final var organization = findOrganizationRepresentationByName(realm.toRepresentation(), "org-home");
                        linkIdentityProviderToOrganization(realm.toRepresentation(), organization, "third-oidc");

                        modifyAuthenticatorConfig(
                                "test-realm",
                                "Copy of browser forms",
                                "ext-auth-home-idp-discovery",
                                Map.entry("forwardToFirstMatch", "false")
                        );


                        final var realmRepresentation = realm.toRepresentation();
                        realmRepresentation.getAttributes().put(ORG_CONFIG_VALIDATE_IDP_KEY, "true");
                        realm.update(realmRepresentation);

                        List<DynamicContainer> dynamicContainers = runCypressTests(
                                thirdIdentityProviderValidated.testPrefix,
                                thirdIdentityProviderValidated.testFile
                        );
                        cleanupKeycloakInstance();
                        return dynamicContainers;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
        }).flatMap(List::stream);
    }

    @TestFactory
    @DisplayName("Testcases when the bypassLoginPage is true")
    public List<DynamicContainer> bypassLoginEnabledTests() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false);
        final var realm = findRealmByName("test-realm");

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
                setupKeycloakInstanceWithMultipleIdpsEnabledAtOrganization();

                if (forwardToFirstMatch.equals(ForwardToFirstMatch.FALSE)) {
                    modifyAuthenticatorConfig(
                            "test-realm",
                            "Copy of browser forms",
                            "ext-auth-home-idp-discovery",
                            Map.entry("forwardToFirstMatch", "false")
                    );
                }
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
    @DisplayName("When the organizaton has mutliple domains and multiple IDPs set up with each IDP linked to different domain that should work correctly")
    public List<DynamicContainer> testIdpsLinkedToOrgWhenDomainsAreSetForIdps() throws IOException, InterruptedException, TimeoutException {
        setupKeycloakInstanceWithMultipleIdpsEnabledAtOrganization();
        final var realm = findRealmByName("test-realm");
        final var organization = findOrganizationRepresentationByName(realm.toRepresentation(), "org-home");
        organization.setDomains(List.of("phasetwo.io", "auth.it"));
        updateOrganizationByRepresentation(organization);

        final var linkToIdp01 = realm.identityProviders().get("oidc-idp").toRepresentation();
        final var linkToIdp02 = realm.identityProviders().get("second-oidc").toRepresentation();

        IdentityProviders.setAttributeMultivalued(
                linkToIdp01.getConfig(),
                ORG_DOMAIN_CONFIG_KEY,
                Set.of("phasetwo.io")
        );

        IdentityProviders.setAttributeMultivalued(
                linkToIdp02.getConfig(),
                ORG_DOMAIN_CONFIG_KEY,
                Set.of("auth.it")
        );

        realm.identityProviders().get(linkToIdp01.getAlias()).update(linkToIdp01);
        realm.identityProviders().get(linkToIdp02.getAlias()).update(linkToIdp02);

        return runCypressTests(
                "Organization's Linked IDP has domain set for each IDP",
                "cypress/e2e/home-idp-organization/multiple-idps-enabled-with-domains-per-idp.cy.ts"
        );
    }

    @TestFactory
    @DisplayName("When a user tries to log in without an organizational idp, but has a linked idp to their user, we should show that")
    public List<DynamicContainer> testIdpsLinkedToUserWhenNoOrgIdpIsSelected() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false);
        List<DynamicContainer> dynamicContainers = runCypressTests(
                "User without organization links his account:",
                "cypress/e2e/home-idp-organization/user-without-org-links-account.cy.ts"
        );
        final var i = 0;
        return dynamicContainers;
    }

    private void setupKeycloakInstanceWithMultipleIdpsEnabledAtOrganization() throws JsonProcessingException {
        setupTestKeycloakInstance(false);
        final var secondClientRealm = importRealm("/realms/external-idp.json", "second-external-idp");
        final var realm = findRealmByName("test-realm");

        RealmRepresentation realmRepresentation = realm.toRepresentation();
        Map<String, String> attributes = new HashMap<>(realmRepresentation.getAttributesOrEmpty());
        realmRepresentation.setAttributes(attributes);
        attributes.put(ORG_CONFIG_MULTIPLE_IDPS_KEY, "true");
        realm.update(realmRepresentation);

        final var secondIdentityProvider = createIdentityProviderIn(realm, "second-oidc", false, "second-external-idp");
        updateRedirectUriInClientRealm(secondClientRealm, secondIdentityProvider);
        final var organization = findOrganizationRepresentationByName(realm.toRepresentation(), "org-home");
        linkIdentityProviderToOrganization(realm.toRepresentation(), organization, "second-oidc");
    }

    private RealmRepresentation importRealm(String jsonRepresentationPath) {
        return importRealm(jsonRepresentationPath, null);
    }

    private RealmRepresentation importRealm(String jsonRepresentationPath, @Nullable String realmOverride) {
        RealmRepresentation realm =
                loadJson(getClass().getResourceAsStream(jsonRepresentationPath),
                        RealmRepresentation.class);
        if (realmOverride != null) {
            realm.setRealm(realmOverride);
        }
        importRealm(realm, keycloak);
        knownRealms.add(realm.getRealm());
        log.info("realm imported successfully:" + realm.getRealm());
        return realm;
    }

    private static RealmResource findRealmByName(String realm) {
        return keycloak
                .realms()
                .realm(realm);
    }

    @TestFactory
    @DisplayName("The UsernameNoteAuthenticator should ask the username, handle extra steps, and continue with HomeIDP Authentication")
    public List<DynamicContainer> testUsernameInAuthNoteFormWithHomeIdpIfTheresExtraAuthenticationFormPartsBetween() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false, "/realms/kc-realm-with-home-idp-with-username-auth-note-flow.json");
        final var cypressResult = runCypressTests(
                "",
                "cypress/e2e/home-idp-organization/auth-note-form-with-home-idp.cy.ts");
        return cypressResult;
    }

    @TestFactory
    @DisplayName("When the HomeIdp requires the verified email config then no home IDP should be presented to them")
    public List<DynamicContainer> testVerifiedEmailOnHomeIdpConfig() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false, "/realms/kc-realm-with-home-idp-flow-and-preregistered-users.json");
        final var cypressResult = runCypressTests(
                "",
                "cypress/e2e/home-idp-organization/home-idp-require-verified-email-test.cy.ts");
        return cypressResult;
    }

    @TestFactory
    @DisplayName("Basic tests for the IdpSelectorAuthenticator Authenticator")
    public List<DynamicContainer> testIdpSelectorAuthenticator() throws IOException, InterruptedException, TimeoutException {
        setupTestKeycloakInstance(false, "/realms/kc-realm-idp-selector.json");
        final var realm = findRealmByName("test-realm");
        final var linkOnlyIdp = realm.identityProviders().get("oidc-idp").toRepresentation();
        linkOnlyIdp.setAlias("link-only-oidc-idp");
        linkOnlyIdp.setInternalId(null);
        linkOnlyIdp.setLinkOnly(true);
        final var responseLinkOnly = realm.identityProviders().create(linkOnlyIdp);
        assertThat(responseLinkOnly.getStatus(), Matchers.is(Response.Status.CREATED.getStatusCode()));
        final var disabledIdp = realm.identityProviders().get("oidc-idp").toRepresentation();
        disabledIdp.setAlias("disabled-oidc-idp");
        disabledIdp.setInternalId(null);
        disabledIdp.setEnabled(false);
        realm.identityProviders().create(disabledIdp);
        final var cypressResult = runCypressTests(
                "",
                "cypress/e2e/home-idp-organization/idp-selector-authenticator-test.cy.ts");
        return cypressResult;
    }

    private void modifyAuthenticatorConfig(String realmName, String flowAlias, String providerId, Map.Entry<String, String> modifiedConfig) {
        final var realm = findRealmByName(realmName);

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
        RealmRepresentation testRealm = importRealm(testRealmRepresentationLocation);

        // import client realm
        RealmRepresentation clientRealm = importRealm("/realms/external-idp.json");

        //create idp
        final var idp = createIdentityProviderIn(keycloak.realm("test-realm"), OIDC_IDP, isIdpLinkOnlyValue, "external-idp");
        updateRedirectUriInClientRealm(clientRealm, idp);

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
        assignEachUserAccountManagementRoles(testRealm);
    }

    private void assignEachUserAccountManagementRoles(RealmRepresentation realmRepresentation) {
        var realm = keycloak.realm(realmRepresentation.getRealm());
        var client = realm
                .clients()
                .findByClientId("account")
                .getFirst();
        var roles = realm
                .clients()
                .get(client.getId())
                .roles()
                .list();
        realm.users().list().forEach(user -> {
            realm
                    .users()
                    .get(user.getId())
                    .roles()
                    .clientLevel(client.getId())
                    .add(roles);
        });
    }

    private static void updateRedirectUriInClientRealm(RealmRepresentation clientRealm, IdentityProviderRepresentation identityProvider) {
        //update redirectUris
        var redirectUri = "http://host.testcontainers.internal:%s/auth/realms/test-realm/broker/%s/endpoint".formatted(container.getHttpPort(), identityProvider.getAlias());
        var clientRep = keycloak.realm(clientRealm.getRealm()).clients().findByClientId("test-realm-client").get(0);
        clientRep.setRedirectUris(List.of(redirectUri));
        keycloak.realm(clientRealm.getRealm()).clients().get(clientRep.getId()).update(clientRep);
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

    private void updateOrganizationByRepresentation(OrganizationRepresentation organizationRepresentation) throws JsonProcessingException {
        var response2 =
                given()
                        .baseUri(container.getAuthServerUrl())
                        .basePath("realms/" + organizationRepresentation.getRealm() + "/orgs/" + organizationRepresentation.getId())
                        .contentType("application/json")
                        .auth()
                        .oauth2(keycloak.tokenManager().getAccessTokenString())
                        .and()
                        .body(toJsonString(organizationRepresentation))
                        .when()
                        .put()
                        .then()
                        .extract()
                        .response();
        assertThat(response2.getStatusCode(), is(Response.Status.NO_CONTENT.getStatusCode()));
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

    private IdentityProviderRepresentation createIdentityProviderIn(RealmResource realm, String providerAlias, Boolean isIdpLinkOnlyValue, String externalIdpId) {
        IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
        idp.setAlias(providerAlias);
        idp.setProviderId("oidc");
        idp.setEnabled(true);
        idp.setFirstBrokerLoginFlowAlias("first broker login");
        idp.setLinkOnly(isIdpLinkOnlyValue);
        idp.setConfig(
                new ImmutableMap.Builder<String, String>()
                        .put("useJwksUrl", "true")
                        .put("syncMode", "FORCE")
                        .put("authorizationUrl", "http://host.testcontainers.internal:%s/auth/realms/%s/protocol/openid-connect/auth".formatted(container.getHttpPort(), externalIdpId))
                        .put("hideOnLoginPage", "")
                        .put("loginHint", "")
                        .put("uiLocales", "")
                        .put("backchannelSupported", "")
                        .put("disableUserInfo", "")
                        .put("acceptsPromptNoneForwardFromClient", "")
                        .put("validateSignature", "")
                        .put("pkceEnabled", "")
                        .put("tokenUrl", "http://host.testcontainers.internal:%s/auth/realms/%s/protocol/openid-connect/token".formatted(container.getHttpPort(), externalIdpId))
                        .put("clientAuthMethod", "client_secret_post")
                        .put("clientId", "test-realm-client")
                        .put("clientSecret", "secret-123")
                        .build());
        Response response = realm.identityProviders().create(idp);
        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        return realm.identityProviders().get(providerAlias).toRepresentation();
    }

    private @NotNull List<DynamicContainer> runCypressTests(String testContainerNamePrefix, String cypressTestFile) throws InterruptedException, TimeoutException, IOException {
        List<DynamicContainer> dynamicContainers = new ArrayList<>();
        Path screenshotDirectory = Path.of("target", "cypress-output", "screenshots");
        Files.createDirectories(screenshotDirectory);
        try (CypressContainer cypressContainer =
                     new CypressContainer()
                             .withBaseUrl(
                                     "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
                             .withLogConsumer(new JbossLogConsumer(log))
                             .withSpec(cypressTestFile)
                             .withFileSystemBind(screenshotDirectory.toAbsolutePath().toString(), "/e2e/cypress/screenshots/", BindMode.READ_WRITE)
                             .withBrowser("electron")) {
            cypressContainer.start();
            CypressTestResults testResults = cypressContainer.getTestResults();
            dynamicContainers.addAll(convertToJUnitDynamicTests(testContainerNamePrefix, testResults));
        }
        return dynamicContainers;
    }

    private static void copyDirFromContainer(CypressContainer cypressContainer, String directoryName) {
        try {
            Files.createDirectories(Path.of("./target/cypress-output", directoryName));
            cypressContainer
                    .execInContainer("ls", "-p", "-1", directoryName)
                    .getStdout()
                    .lines()
                    .forEach(filepath -> {
                        String fullFilePath = directoryName + filepath;
                        if (filepath.endsWith("/")) {
                            copyDirFromContainer(cypressContainer, fullFilePath);
                        } else {
                            cypressContainer.copyFileFromContainer(fullFilePath, "./target/cypress-output/%s".formatted(fullFilePath));
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
