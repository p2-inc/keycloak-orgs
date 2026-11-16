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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.testcontainers.Testcontainers;

/**
 * E2E test for GHSA-gvcc-4vw2-6wvr: an organization IdP must not be able to create (or let a member
 * create) accounts for email addresses outside the organization's domains.
 *
 * <p>Setup:
 *
 * <ul>
 *   <li>{@code test-realm} with a public client; org {@code acme} owns domain {@code phasetwo.io}
 *   <li>{@code external-idp} realm acting as the upstream OIDC IdP with two users:
 *       {@code test@phasetwo.io} (in-domain) and {@code test@auth.it} (out-of-domain), both
 *       {@code test123}
 *   <li>{@code org-idp} in test-realm links to external-idp, {@code syncMode=FORCE}, using the realm
 *       default {@code first broker login} flow (into which {@code ext-auth-org-domain-fbl-verifier}
 *       is auto-wired on realm create)
 *   <li>the {@code idp-review-profile} step is forced on so the email field is editable, to exercise
 *       the "edit the email on the Review Profile page" variant
 * </ul>
 *
 * <p>Scenarios (in {@code first-broker-domain-verifier.cy.ts}):
 *
 * <ol>
 *   <li>IdP supplies an out-of-domain email → first broker login blocked, no account created
 *   <li>member edits the email to an out-of-domain address on Review Profile → blocked, no account
 *   <li>in-domain email (control) → first broker login succeeds, account created
 * </ol>
 */
@EnabledIfSystemProperty(named = "include.cypress", matches = "true")
@org.testcontainers.junit.jupiter.Testcontainers
class CypressFirstBrokerDomainVerifierTest extends AbstractCypressOrganizationTest {

  private static final String IDP_ALIAS = "org-idp";

  @TestFactory
  List<DynamicContainer> runFirstBrokerDomainVerifierTests()
      throws IOException, InterruptedException, TimeoutException {

    Testcontainers.exposeHostPorts(container.getHttpPort());
    setupTestEnv();

    try (CypressContainer cypressContainer =
        new CypressContainer()
            .withBaseUrl(
                "http://host.testcontainers.internal:" + container.getHttpPort() + "/auth/")
            .withSpec("cypress/e2e/first-broker-domain-verifier/first-broker-domain-verifier.cy.ts")
            .withBrowser("electron")) {
      cypressContainer.start();
      CypressTestResults results = cypressContainer.getTestResults();
      cleanupKeycloakInstance();
      return convertToJUnitDynamicTests(results);
    }
  }

  private void setupTestEnv() throws IOException {
    // test realm (public client) + upstream IdP realm
    RealmRepresentation testRealm =
        importRealm("/realms/kc-realm-account-hint-post-broker.json", null);
    RealmRepresentation externalIdpRealm = importRealm("/realms/external-idp.json", null);

    // IdP in test-realm -> external-idp, using the realm default "first broker login" flow
    IdentityProviderRepresentation idp = buildIdpRepresentation(IDP_ALIAS, "external-idp");
    var createIdpResp = keycloak.realm("test-realm").identityProviders().create(idp);
    assertThat(createIdpResp.getStatus(), is(Response.Status.CREATED.getStatusCode()));
    idp = keycloak.realm("test-realm").identityProviders().get(IDP_ALIAS).toRepresentation();
    updateRedirectUri(externalIdpRealm, idp);

    // org acme owns phasetwo.io; link the IdP to it (records org ownership, FORCE sync)
    OrganizationRepresentation acme =
        createOrganization(
            testRealm,
            new OrganizationRepresentation().name("acme").domains(List.of("phasetwo.io")));
    linkIdpToOrg(testRealm, acme, IDP_ALIAS);

    // force the Review Profile page so the email field is editable (variant 2)
    forceReviewProfile(testRealm);
  }

  private IdentityProviderRepresentation buildIdpRepresentation(
      String alias, String externalRealmId) {
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias(alias);
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    // realm default flow; the domain verifier is auto-wired into it on realm create
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

  private void linkIdpToOrg(
      RealmRepresentation testRealm, OrganizationRepresentation org, String idpAlias)
      throws com.fasterxml.jackson.core.JsonProcessingException {
    LinkIdp link = new LinkIdp();
    link.setAlias(idpAlias);
    link.setSyncMode("FORCE");
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

  private void forceReviewProfile(RealmRepresentation testRealm) {
    var flows = keycloak.realm(testRealm.getRealm()).flows();
    AuthenticationExecutionInfoRepresentation reviewProfile =
        flows.getExecutions("first broker login").stream()
            .filter(e -> "idp-review-profile".equals(e.getProviderId()))
            .findFirst()
            .orElseThrow();
    var cfg = new org.keycloak.representations.idm.AuthenticatorConfigRepresentation();
    cfg.setAlias("force-review");
    cfg.setConfig(Map.of("update.profile.on.first.login", "on"));
    flows.newExecutionConfig(reviewProfile.getId(), cfg);
  }
}
