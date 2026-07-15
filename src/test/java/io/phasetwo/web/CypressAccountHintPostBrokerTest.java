package io.phasetwo.web;

import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableMap;
import io.github.wimdeblauwe.testcontainers.cypress.CypressContainer;
import io.github.wimdeblauwe.testcontainers.cypress.CypressTestResults;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
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
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.FederatedIdentityRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;

/**
 * E2E test: account_hint survives post-broker login.
 *
 * Prior to this test, ext-select-org was never invoked during the post-broker-login phase in
 * any existing test -- phasetwo's default post-broker flow ("post org broker login") only does
 * org bookkeeping (verify idp, add user to org, set org note) and does not include ext-select-org.
 * The interaction between account_hint and a federated (IdP) login therefore had zero coverage
 * before this test: existing select-organization-by-id.cy.ts / select-organization-by-name.cy.ts
 * only exercise account_hint / prompt=select_account for direct username/password login.
 *
 * Setup:
 *   - test-realm with home-IdP discovery browser flow
 *   - external-idp realm as OIDC IdP (user: test@phasetwo.io / test123)
 *   - org-1 (domain phasetwo.io) linked to oidc-idp, with postBrokerLoginFlowAlias set to a
 *     custom flow (createCustomPostBrokerLoginFlow) that adds ext-select-org after the default
 *     org bookkeeping steps -- without it, account_hint is never evaluated post-broker
 *   - org-2 (domain org2.com)
 *   - idp-test-user member of both orgs, federated identity linked to oidc-idp by the external
 *     realm's actual user id (not email, which Keycloak doesn't recognize as pre-linked)
 *
 * Scenarios exercised by the Cypress spec:
 *   1. Login via IdP + account_hint=org1 → org-1 auto-selected, no picker
 *   2. Login via IdP + account_hint=org2 → org-2 auto-selected, no picker
 *   3. Login via IdP, no hint, 2 orgs     → org picker shown
 *   4. Login via IdP + account_hint=bad   → invalidOrganizationError
 */
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressAccountHintPostBrokerTest extends AbstractCypressOrganizationTest {

  private static final String IDP_ALIAS = "oidc-idp";
  private static final String IDP_USER_EMAIL = "test@phasetwo.io";
  private static final String IDP_USER_PASS = "test123";
  // ext-select-org isn't part of phasetwo's default "post org broker login" flow (org
  // bookkeeping only), so it must be added to a custom post-broker-login flow for the
  // account_hint fix to actually be exercised after a federated login.
  private static final String POST_BROKER_FLOW_ALIAS = "custom-post-broker-login";

  @TestFactory
  List<DynamicContainer> runAccountHintPostBrokerTests()
      throws IOException, InterruptedException, TimeoutException {

    Testcontainers.exposeHostPorts(container.getHttpPort());
    setupTestEnv();

    try (CypressContainer cypressContainer =
        new CypressContainer()
            .withBaseUrl(
                "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
            .withSpec("cypress/e2e/select-organization/account-hint-post-broker.cy.ts")
            .withBrowser("electron")) {
      cypressContainer.start();
      CypressTestResults results = cypressContainer.getTestResults();
      cleanupKeycloakInstance();
      return convertToJUnitDynamicTests(results);
    }
  }

  // ── setup ─────────────────────────────────────────────────────────────────

  private void setupTestEnv() throws IOException {
    // 1. import test realm (home-IdP discovery browser flow + public-client)
    RealmRepresentation testRealm =
        importRealm("/realms/kc-realm-account-hint-post-broker.json", null);

    // 2. import external IdP realm
    RealmRepresentation externalIdpRealm = importRealm("/realms/external-idp.json", null);

    // 3. create IdP in test-realm pointing at external-idp realm
    IdentityProviderRepresentation idp = buildIdpRepresentation(IDP_ALIAS, "external-idp");
    var createIdpResp = keycloak.realm("test-realm").identityProviders().create(idp);
    assertThat(createIdpResp.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    // re-read to get full representation (including internalId)
    idp = keycloak.realm("test-realm").identityProviders().get(IDP_ALIAS).toRepresentation();

    // update redirect URI in external-idp client to match container port
    updateRedirectUri(externalIdpRealm, idp);

    // 4. create org-1 with domain phasetwo.io
    OrganizationRepresentation org1 =
        createOrganization(
            testRealm,
            new OrganizationRepresentation().name("org-1").domains(List.of("phasetwo.io")));

    // 5. create org-2 with domain org2.com
    OrganizationRepresentation org2 =
        createOrganization(
            testRealm, new OrganizationRepresentation().name("org-2").domains(List.of("org2.com")));

    // 6. create a custom post-broker-login flow (org bookkeeping + ext-select-org) and link
    // the IdP to org-1 using it, so account_hint is actually evaluated after federated login
    createCustomPostBrokerLoginFlow(testRealm);
    linkIdpToOrg(testRealm, org1, IDP_ALIAS);

    // 7. create the IdP user in test-realm so org membership can be set before first browser login
    String userId = createIdpUserInTestRealm(testRealm);

    // 8. link federated identity so IdP login maps to this user
    addFederatedIdentity(testRealm, userId);

    // 9. add user to both orgs
    addMembership(testRealm, org1.getId(), userId);
    addMembership(testRealm, org2.getId(), userId);

    // 10. grant account-console roles so the account page loads
    assignAccountRoles(testRealm, userId);
  }

  private IdentityProviderRepresentation buildIdpRepresentation(
      String alias, String externalRealmId) {
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias(alias);
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("useJwksUrl", "true")
            .put("syncMode", "FORCE")
            .put(
                "authorizationUrl",
                "http://host.testcontainers.internal:%s/auth/realms/%s/protocol/openid-connect/auth"
                    .formatted(container.getHttpPort(), externalRealmId))
            .put(
                "tokenUrl",
                "http://host.testcontainers.internal:%s/auth/realms/%s/protocol/openid-connect/token"
                    .formatted(container.getHttpPort(), externalRealmId))
            .put("clientAuthMethod", "client_secret_post")
            .put("clientId", "test-realm-client")
            .put("clientSecret", "secret-123")
            .put("hideOnLoginPage", "")
            .put("loginHint", "")
            .put("validateSignature", "")
            .put("pkceEnabled", "")
            .build());
    return idp;
  }

  private void updateRedirectUri(
      RealmRepresentation externalIdpRealm, IdentityProviderRepresentation idp) {
    String redirectUri =
        "http://host.testcontainers.internal:%s/auth/realms/test-realm/broker/%s/endpoint"
            .formatted(container.getHttpPort(), idp.getAlias());
    var clientRep =
        keycloak
            .realm(externalIdpRealm.getRealm())
            .clients()
            .findByClientId("test-realm-client")
            .get(0);
    clientRep.setRedirectUris(List.of(redirectUri));
    keycloak.realm(externalIdpRealm.getRealm()).clients().get(clientRep.getId()).update(clientRep);
  }

  private void createCustomPostBrokerLoginFlow(RealmRepresentation testRealm) {
    var flowsRes = keycloak.realm(testRealm.getRealm()).flows();

    AuthenticationFlowRepresentation flow = new AuthenticationFlowRepresentation();
    flow.setAlias(POST_BROKER_FLOW_ALIAS);
    flow.setProviderId("basic-flow");
    flow.setTopLevel(true);
    flow.setBuiltIn(false);
    flowsRes.createFlow(flow);

    for (String provider :
        List.of(
            "ext-auth-org-note",
            "ext-auth-org-add-user",
            "ext-auth-validate-idp",
            "ext-select-org")) {
      Map<String, Object> params = new HashMap<>();
      params.put("provider", provider);
      flowsRes.addExecution(POST_BROKER_FLOW_ALIAS, params);
    }

    for (AuthenticationExecutionInfoRepresentation execution :
        flowsRes.getExecutions(POST_BROKER_FLOW_ALIAS)) {
      execution.setRequirement("REQUIRED");
      flowsRes.updateExecutions(POST_BROKER_FLOW_ALIAS, execution);
    }
  }

  private void linkIdpToOrg(
      RealmRepresentation testRealm, OrganizationRepresentation org, String idpAlias)
      throws com.fasterxml.jackson.core.JsonProcessingException {
    LinkIdp link = new LinkIdp();
    link.setAlias(idpAlias);
    link.setSyncMode("IMPORT");
    link.setPostBrokerFlow(POST_BROKER_FLOW_ALIAS);
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

  private String createIdpUserInTestRealm(RealmRepresentation testRealm) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(IDP_USER_EMAIL);
    user.setEmail(IDP_USER_EMAIL);
    user.setEmailVerified(true);
    user.setEnabled(true);
    user.setFirstName("Test");
    user.setLastName("User");
    // password credential allows validateActiveOrganization to get a token via direct grant
    org.keycloak.representations.idm.CredentialRepresentation cred =
        new org.keycloak.representations.idm.CredentialRepresentation();
    cred.setType("password");
    cred.setValue(IDP_USER_PASS);
    cred.setTemporary(false);
    user.setCredentials(List.of(cred));

    var resp = keycloak.realm(testRealm.getRealm()).users().create(user);
    assertThat(resp.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    String location = resp.getHeaderString("Location");
    return location.substring(location.lastIndexOf("/") + 1);
  }

  private void addFederatedIdentity(RealmRepresentation testRealm, String userId) {
    // must be the external realm's actual user id (matches the OIDC "sub" claim the broker
    // receives), not the email -- otherwise Keycloak doesn't recognize the incoming federated
    // login as already-linked and runs first-broker-login ("Account already exists") instead
    String externalUserId =
        keycloak.realm("external-idp").users().searchByEmail(IDP_USER_EMAIL, true).get(0).getId();

    FederatedIdentityRepresentation fedId = new FederatedIdentityRepresentation();
    fedId.setIdentityProvider(IDP_ALIAS);
    fedId.setUserId(externalUserId);
    fedId.setUserName(IDP_USER_EMAIL);
    var resp =
        keycloak
            .realm(testRealm.getRealm())
            .users()
            .get(userId)
            .addFederatedIdentity(IDP_ALIAS, fedId);
    assertThat(resp.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
  }

  private void addMembership(RealmRepresentation testRealm, String orgId, String userId)
      throws com.fasterxml.jackson.core.JsonProcessingException {
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

  private void assignAccountRoles(RealmRepresentation testRealm, String userId) {
    var realmRes = keycloak.realm(testRealm.getRealm());
    var accountClient = realmRes.clients().findByClientId("account").getFirst();
    var roles = realmRes.clients().get(accountClient.getId()).roles().list();
    realmRes.users().get(userId).roles().clientLevel(accountClient.getId()).add(roles);
  }
}
