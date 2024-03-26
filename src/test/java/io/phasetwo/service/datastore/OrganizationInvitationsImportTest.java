package io.phasetwo.service.datastore;

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
import io.phasetwo.service.datastore.representation.InvitationRepresentation;
import io.phasetwo.service.datastore.representation.KeycloakOrgsRealmRepresentation;
import io.phasetwo.service.representation.Invitation;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@JBossLog
public class OrganizationInvitationsImportTest extends AbstractOrganizationTest {

  @Test
  void testOrganizationInvitationsImport() throws IOException {
    // prepare data
    var realm = "org-realm";
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-invitations-import-test.json"),
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
              validateOrgInvitations(org1.getInvitations(), organizationRepresentation, realm);
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
              validateOrgInvitations(org2.getInvitations(), organizationRepresentation, realm);
            });

    // users
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(2));

    // delete realm
    keycloak.realm(realm).remove();
  }

  @Test
  void testOrganizationInvitationsExistingMemberFailedImport() throws IOException {
    // prepare data
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream("/orgs/org-invitations-import-existing-member-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var response = importRealm(testRealm, keycloak);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationInvitationsInviterMissingFailedImport() throws IOException {
    // prepare data
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream("/orgs/org-invitations-import-inviter-missing-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var response = importRealm(testRealm, keycloak);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationInvitationsInviterNotMemberOfOrgFailedImport() throws IOException {
    // prepare data
    KeycloakOrgsRealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream("/orgs/org-invitations-import-inviter-not-in-org-test.json"),
            KeycloakOrgsRealmRepresentation.class);

    // import realm
    var response = importRealm(testRealm, keycloak);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  private void validateOrgInvitations(
      List<InvitationRepresentation> importInvitations,
      OrganizationRepresentation organizationRepresentation,
      String realm) {

    var invitationsResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath(
                "realms/" + realm + "/orgs/" + organizationRepresentation.getId() + "/invitations")
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
      List<Invitation> invitations =
          objectMapper()
              .readValue(invitationsResponse.getBody().asString(), new TypeReference<>() {});

      // validate invitations and parameters
      assertThat(importInvitations, hasSize(invitations.size()));
      invitations.forEach(
          invitation -> {
            var importedInvitation =
                importInvitations.stream()
                    .filter(invitationRep -> invitation.getEmail().equals(invitationRep.getEmail()))
                    .findFirst()
                    .orElseThrow();
            validateInvitationParameters(importedInvitation, invitation, realm);
          });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private void validateInvitationParameters(
      InvitationRepresentation invitationRepresentation, Invitation invitation, String realm) {
    var inviter =
        keycloak
            .realm(realm)
            .users()
            .searchByUsername(invitationRepresentation.getInviterUsername(), true)
            .stream()
            .findFirst()
            .orElseThrow();
    assertThat(inviter.getId(), Matchers.is(invitation.getInviterId()));
    assertThat(
        invitationRepresentation.getRedirectUri(), Matchers.is(invitation.getInvitationUrl()));
    assertThat(invitationRepresentation.getEmail(), Matchers.is(invitation.getEmail()));

    if (invitationRepresentation.getRoles().isEmpty()) {
      assertThat(invitationRepresentation.getRoles(), hasSize(invitation.getRoles().size()));
    } else {
      assertThat(
          invitationRepresentation.getRoles(), containsInAnyOrder(invitation.getRoles().toArray()));
    }

    if (invitationRepresentation.getAttributes().isEmpty()) {
      assertThat(invitationRepresentation.getAttributes().entrySet(), hasSize(0));
    } else {
      assertThat(
          invitationRepresentation.getAttributes().entrySet(),
          containsInAnyOrder(invitation.getAttributes().entrySet().toArray()));
    }
  }
}
