package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.loadJson;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.datastore.representation.KeycloakOrgsRealmRepresentation;
import io.phasetwo.service.datastore.representation.OrganizationAttributes;
import io.phasetwo.service.datastore.representation.OrganizationRoleRepresentation;
import io.phasetwo.service.representation.OrganizationRole;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@JBossLog
public class KeycloakOrganizationImportTest extends AbstractOrganizationTest {

  @Test
  void testStandardKeycloakRealmImport() throws IOException {
    // prepare data
    var realm = "org-realm";
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/standard-keycloak-import-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var importRealmResponse = importRealm(testRealm, keycloak);
    assertThat(
        importRealmResponse.getStatusCode(),
        CoreMatchers.is(Response.Status.CREATED.getStatusCode()));

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

    // delete realm
    keycloak.realm(realm).remove();
  }

  @Test
  void testOrganizationImport() throws IOException {
    // prepare data
    var realm = "org-realm";
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-import-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var importRealmResponse = importRealm(testRealm, keycloak);
    assertThat(
        importRealmResponse.getStatusCode(),
        CoreMatchers.is(Response.Status.CREATED.getStatusCode()));

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
    assertThat(organizations, hasSize(2));

    // test org1
    var org1 =
        testRealm.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test1"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrg(org1.getOrganization(), organizationRepresentation);
              validateRoles(org1, organizationRepresentation, realm);
              validateIdpLink(org1, organizationRepresentation, realm);
            });

    // test org2
    var org2 =
        testRealm.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test2"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrg(org2.getOrganization(), organizationRepresentation);
              validateRoles(org2, organizationRepresentation, realm);
            });

    // users
    assertThat(keycloak.realm(realm).users().count(), Matchers.is(2));
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(2));

    // delete realm
    keycloak.realm(realm).remove();
  }

  private void validateIdpLink(
      io.phasetwo.service.datastore.representation.OrganizationRepresentation org,
      OrganizationRepresentation organizationRepresentation,
      String realm) {
    var identityProvider =
        keycloak.realm(realm).identityProviders().get(org.getIdpLink()).toRepresentation();

    assertThat(
        identityProvider.getConfig().get(ORG_OWNER_CONFIG_KEY),
        is(organizationRepresentation.getId()));
  }

  private void validateRoles(
      io.phasetwo.service.datastore.representation.OrganizationRepresentation org,
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

  private static void validateOrg(
      OrganizationAttributes organizationAttributes, OrganizationRepresentation rep) {
    assertThat(organizationAttributes.getName(), Matchers.is(rep.getName()));

    if (rep.getDomains().isEmpty()) {
      assertThat(organizationAttributes.getDomains(), hasSize(0));
    } else {
      assertThat(organizationAttributes.getDomains(), contains(rep.getDomains().toArray()));
    }
    assertThat(organizationAttributes.getUrl(), Matchers.is(rep.getUrl()));
    assertThat(organizationAttributes.getDisplayName(), Matchers.is(rep.getDisplayName()));
    if (rep.getAttributes().isEmpty()) {
      assertThat(organizationAttributes.getAttributes().entrySet(), hasSize(0));
    } else {
      assertThat(
          organizationAttributes.getAttributes().entrySet(),
          contains(rep.getAttributes().entrySet().toArray()));
    }
  }
}
