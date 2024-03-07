package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.loadJson;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.datastore.representation.KeycloakOrgsRealmRepresentation;
import io.phasetwo.service.datastore.representation.UserRolesRepresentation;
import io.phasetwo.service.representation.OrganizationRole;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class KeycloakOrganizationMembersImportTest extends AbstractOrganizationTest {

  @Test
  void testOrganizationMembersImport() throws IOException {
    // prepare data
    var realm = "org-realm";
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-members-import-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var importRealmResponse = importRealm(testRealm, keycloak);
    assertThat(importRealmResponse.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));

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
              validateOrgMembers(org1.getMembers(), organizationRepresentation, realm);
            });

    // test org2
    var org2 =
        testRealm.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test2"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test2"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrgMembers(org2.getMembers(), organizationRepresentation, realm);
            });

    // users
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(2));

    // delete realm
    keycloak.realm(realm).remove();
  }

  @Test
  void testOrganizationMemberImportMissingUser() throws IOException {
    // prepare data
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-member-import-missing-user-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var response = importRealm(testRealm, keycloak);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  private void validateOrgMembers(
      List<UserRolesRepresentation> importedMembers,
      OrganizationRepresentation organizationRepresentation,
      String realm) {

    var membersResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath(
                "realms/" + realm + "/orgs/" + organizationRepresentation.getId() + "/members")
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
      List<UserRepresentation> members =
          objectMapper().readValue(membersResponse.getBody().asString(), new TypeReference<>() {});

      var managedUsers =
          members.stream()
              .filter(
                  member ->
                      !String.format("org-admin-%s", organizationRepresentation.getId())
                          .equals(member.getUsername()))
              .toList();

      assertThat(managedUsers, hasSize(importedMembers.size()));
      assertThat(
          managedUsers.stream().map(UserRepresentation::getUsername).toList(),
          containsInAnyOrder(
              importedMembers.stream().map(UserRolesRepresentation::getUsername).toArray()));

      // validate roles
      managedUsers.forEach(
          userRepresentation -> {
            var member =
                importedMembers.stream()
                    .filter(
                        importedMember ->
                            importedMember.getUsername().equals(userRepresentation.getUsername()))
                    .findFirst()
                    .orElseThrow();
            validateOrgRoles(userRepresentation, member, organizationRepresentation.getId(), realm);
          });

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private void validateOrgRoles(
      UserRepresentation userRepresentation,
      UserRolesRepresentation member,
      String orgId,
      String realm) {

    var rolesResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath(
                "realms/"
                    + realm
                    + "/users/"
                    + userRepresentation.getId()
                    + "/orgs/"
                    + orgId
                    + "/roles")
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

      assertThat(roles, hasSize(member.getRoles().size()));
      assertThat(
          roles.stream().map(OrganizationRole::getName).toList(),
          containsInAnyOrder(member.getRoles().toArray()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
