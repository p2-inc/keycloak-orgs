package io.phasetwo.web;

import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.representation.LinkIdp;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;

/**
 * E2E: organization-aware login for a multi-tenant store platform.
 *
 * <p>Every store is an organization (globex, initech, acme). The app always sends the store it is
 * opening as {@code account_hint}. Rule under test: a user gets in only as a member of that store
 * or with a pending invitation to it, whatever the login method.
 *
 * <p>Setup:
 *
 * <ul>
 *   <li>test-realm (kc-realm-org-login-use-cases.json): browser flow "shop login" with
 *       ext-inv-auto-join + ext-select-org after the session cookie and after home-IdP discovery
 *       or password; post-broker flows "company sso post broker" (ext-auth-org-add-user,
 *       ext-inv-auto-join, ext-select-org) and "external idp post broker" (ext-inv-auto-join,
 *       ext-select-org)
 *   <li>shophost-upstream realm (org-login-use-cases-upstream.json) stands in for Globex's and
 *       Acme's company SSO, Google, and a partner IdP
 *   <li>globex-sso and acme-sso are linked to their store with the company post-broker flow and
 *       routed by email domain; google and partner use the external post-broker flow
 *   <li>paula: globex member; ivan and sam: invited to globex; carl: globex + initech; pete:
 *       partner user pre-linked and member of globex; nina, zoe, quinn: no store
 * </ul>
 *
 * <p>The Cypress spec covers: account_hint across password, social, company SSO; a contractor
 * picking and switching stores; kc_idp_hint plus account_hint; and refusals.
 */
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressOrgLoginUseCasesTest extends AbstractCypressOrganizationTest {

  private static final String REALM_NAME = "test-realm";
  private static final String UPSTREAM_REALM = "shophost-upstream";
  private static final String COMPANY_POST_BROKER = "company sso post broker";
  private static final String EXTERNAL_POST_BROKER = "external idp post broker";

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

  // ── setup ─────────────────────────────────────────────────────────────────

  private void setupTestEnv() throws IOException {
    RealmRepresentation testRealm = importRealm("/realms/kc-realm-org-login-use-cases.json", null);
    importRealm("/realms/org-login-use-cases-upstream.json", null);

    for (String alias : List.of("globex-sso", "acme-sso")) {
      createIdp(alias, null, true);
    }
    createIdp("google", EXTERNAL_POST_BROKER, false);
    createIdp("partner", EXTERNAL_POST_BROKER, true);

    OrganizationRepresentation globex = createStore(testRealm, "globex", "globex.test");
    OrganizationRepresentation initech = createStore(testRealm, "initech", "initech.test");
    OrganizationRepresentation acme = createStore(testRealm, "acme", "acme.test");

    // company SSO owned by each store, routed by email domain
    linkIdpToOrg(testRealm, globex, "globex-sso", "globex.test");
    linkIdpToOrg(testRealm, acme, "acme-sso", "acme.test");

    addMembership(testRealm, globex.getId(), userId("paula@mail.test"));
    addMembership(testRealm, globex.getId(), userId("carl@contractor.test"));
    addMembership(testRealm, initech.getId(), userId("carl@contractor.test"));

    // pete already has an account linked to the partner IdP, and is a globex member
    String peteId = createLinkedUser("pete@partner.test", "partner");
    addMembership(testRealm, globex.getId(), peteId);

    invite(testRealm, globex.getId(), "ivan@mail.test");
    invite(testRealm, globex.getId(), "sam@gmail.test");
  }

  private void createIdp(String alias, String postBrokerFlow, boolean hideOnLogin) {
    String upstream =
        "http://host.testcontainers.internal:%s/auth/realms/%s/protocol/openid-connect"
            .formatted(container.getHttpPort(), UPSTREAM_REALM);
    Map<String, String> config = new HashMap<>();
    config.put("clientId", alias);
    config.put("clientSecret", "secret-123");
    config.put("clientAuthMethod", "client_secret_post");
    config.put("authorizationUrl", upstream + "/auth");
    config.put("tokenUrl", upstream + "/token");
    config.put("defaultScope", "openid email profile");
    config.put("syncMode", "IMPORT");

    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias(alias);
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setTrustEmail(true);
    idp.setHideOnLogin(hideOnLogin);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    if (postBrokerFlow != null) {
      idp.setPostBrokerLoginFlowAlias(postBrokerFlow);
    }
    idp.setConfig(config);
    var resp = keycloak.realm(REALM_NAME).identityProviders().create(idp);
    assertThat(resp.getStatus(), is(Response.Status.CREATED.getStatusCode()));
  }

  private OrganizationRepresentation createStore(
      RealmRepresentation testRealm, String name, String domain) throws IOException {
    return createOrganization(
        testRealm, new OrganizationRepresentation().name(name).domains(List.of(domain)));
  }

  private void linkIdpToOrg(
      RealmRepresentation testRealm, OrganizationRepresentation org, String alias, String domain)
      throws IOException {
    LinkIdp link = new LinkIdp();
    link.setAlias(alias);
    link.setSyncMode("IMPORT");
    // without an explicit flow the link falls back to "post org broker login", which adds the
    // user to the org but has no ext-select-org, so account_hint would not be checked
    link.setPostBrokerFlow(COMPANY_POST_BROKER);
    link.setDomains(java.util.Set.of(domain));
    var resp =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + testRealm.getRealm() + "/orgs/" + org.getId() + "/idps/link")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .body(toJsonString(link))
            .when()
            .post()
            .then()
            .extract()
            .response();
    assertThat(resp.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));
  }

  private String userId(String email) {
    return keycloak.realm(REALM_NAME).users().searchByEmail(email, true).get(0).getId();
  }

  private String createLinkedUser(String email, String idpAlias) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(email);
    user.setEmail(email);
    user.setEmailVerified(true);
    // a complete profile, otherwise the login stops at the VERIFY_PROFILE required action
    user.setFirstName("Pete");
    user.setLastName("Partner");
    user.setEnabled(true);
    var resp = keycloak.realm(REALM_NAME).users().create(user);
    assertThat(resp.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    String location = resp.getHeaderString("Location");
    String id = location.substring(location.lastIndexOf("/") + 1);

    // must be the upstream realm's user id (the OIDC "sub"), not the email
    String upstreamId =
        keycloak.realm(UPSTREAM_REALM).users().searchByEmail(email, true).get(0).getId();
    FederatedIdentityRepresentation fedId = new FederatedIdentityRepresentation();
    fedId.setIdentityProvider(idpAlias);
    fedId.setUserId(upstreamId);
    fedId.setUserName(email);
    var linkResp = keycloak.realm(REALM_NAME).users().get(id).addFederatedIdentity(idpAlias, fedId);
    assertThat(linkResp.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
    return id;
  }

  private void addMembership(RealmRepresentation testRealm, String orgId, String userId)
      throws IOException {
    var resp =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + testRealm.getRealm() + "/orgs/" + orgId + "/members/" + userId)
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .body(toJsonString(""))
            .when()
            .put()
            .then()
            .extract()
            .response();
    assertThat(resp.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));
  }

  private void invite(RealmRepresentation testRealm, String orgId, String email)
      throws IOException {
    InvitationRequest invitation = new InvitationRequest().email(email).send(false);
    var resp =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + testRealm.getRealm() + "/orgs/" + orgId + "/invitations")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .body(toJsonString(invitation))
            .when()
            .post()
            .then()
            .extract()
            .response();
    assertThat(resp.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));
  }
}
