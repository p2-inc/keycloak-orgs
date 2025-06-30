package io.phasetwo.service.importexport;

import static io.phasetwo.service.Helpers.loadJson;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import io.phasetwo.service.importexport.representation.OrganizationRoleRepresentation;
import io.phasetwo.service.representation.OrganizationRole;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;

@JBossLog
public class OrganizationImportTest extends AbstractOrganizationTest {

  private final String realm = "org-realm";

  @Test
  void emptyOrganizationsImport() throws IOException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/keycloak-realm-no-identity-provider.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/import-empty-organizations-test.json"),
            KeycloakOrgsRepresentation.class);
    // import orgs
    var orgsResponse = importOrgs(orgsRepresentation, keycloak, realm);

    assertThat(
        orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.NO_CONTENT.getStatusCode()));

    // validate
    // get organizations
    var response =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();

    List<OrganizationRepresentation> organizations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(organizations, hasSize(0));
  }

  @Test
  void testOrganizationImport() throws IOException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/keycloak-realm-with-identity-provider.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-import-test.json"),
            KeycloakOrgsRepresentation.class);
    var orgsResponse = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.OK.getStatusCode()));

    // validate
    // get organizations
    var response =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();

    List<OrganizationRepresentation> organizations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(organizations, hasSize(3));

    // test org1
    var org1 =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test1"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrg(org1, organizationRepresentation);
              validateRoles(org1, organizationRepresentation, realm);
              validateIdpLink(org1, organizationRepresentation, realm);
            });

    // test org2
    var org2 =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test2"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrg(org2, organizationRepresentation);
              validateRoles(org2, organizationRepresentation, realm);
            });

    // test organization with custom id
    String customId = "0196afd7-8776-76aa-84d3-a7d7ec7f31a8";
    var organizationWithCustomId =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("Organization with custom ID"))
            .findFirst()
            .orElseThrow();
    assertThat(organizationWithCustomId.getOrganization().getId(), is(customId));

    // users
    assertThat(keycloak.realm(realm).users().count(), Matchers.is(3));
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(3));
  }

  @Test
  void testOrganizationImportMissingIdpConfig() {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/keycloak-realm-no-identity-provider.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-import-test.json"),
            KeycloakOrgsRepresentation.class);
    var orgsResponse = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(
        orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationImportMissingIdpConfigSkipMissingIdp() throws JsonProcessingException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/keycloak-realm-no-identity-provider.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-import-test.json"),
            KeycloakOrgsRepresentation.class);
    var orgsResponse = importOrgSkipMissingIdp(orgsRepresentation, keycloak, realm);
    assertThat(orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.OK.getStatusCode()));

    // validate
    // get organizations
    var response =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();

    List<OrganizationRepresentation> organizations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(organizations, hasSize(3));

    // test org1
    var org1 =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test1"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrg(org1, organizationRepresentation);
              validateRoles(org1, organizationRepresentation, realm);
            });

    // test org2
    var org2 =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test2"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrg(org2, organizationRepresentation);
              validateRoles(org2, organizationRepresentation, realm);
            });

    // users
    assertThat(keycloak.realm(realm).users().count(), Matchers.is(3));
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(3));
  }

  private void validateIdpLink(
      io.phasetwo.service.importexport.representation.OrganizationRepresentation org,
      OrganizationRepresentation organizationRepresentation,
      String realm) {
    var identityProvider =
        keycloak.realm(realm).identityProviders().get(org.getIdpLink()).toRepresentation();

    assertThat(
        identityProvider.getConfig().get(ORG_OWNER_CONFIG_KEY),
        is(organizationRepresentation.getId()));
  }

  private void validateRoles(
      io.phasetwo.service.importexport.representation.OrganizationRepresentation org,
      OrganizationRepresentation organizationRepresentation,
      String realm) {
    // roles
    var rolesResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs/" + organizationRepresentation.getId() + "/roles")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();
    try {
      List<OrganizationRole> roles =
          objectMapper().readValue(rolesResponse.getBody().asString(), new TypeReference<>() {});
      assertThat(
          org.getRoles().stream().map(OrganizationRoleRepresentation::getName).toList(),
          containsInAnyOrder(roles.stream().map(OrganizationRole::getName).toArray()));

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  public void afterEach() {
    // delete realm
    keycloak.realm(realm).remove();
  }
}
