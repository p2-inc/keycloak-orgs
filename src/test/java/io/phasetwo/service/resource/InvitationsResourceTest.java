package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.representation.UserWithOrgs;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

@JBossLog
public class InvitationsResourceTest extends AbstractOrganizationTest {

  @Test
  void organizationInvitationsShouldBeAvailableIfSenderUserIsDeleted() throws IOException {
    // create organization
    var organization =
        createOrganization(
            new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));

    // create role for organization
    createOrgRole(organization.getId(), "example-role");

    // create a inviter user
    UserRepresentation user = createUserWithCredentials(keycloak, REALM, "user1", "pass");
    // add membership
    putRequest("foo", organization.getId(), "members", user.getId());
    // grant rep admin permissions
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      grantUserRole(organization.getId(), role, user.getId());
    }
    Keycloak kc1 = getKeycloak(REALM, "admin-cli", "user1", "pass");

    // create invitation
    InvitationRequest inv =
        new InvitationRequest().email("johndoe@example.com").role("example-role");
    var response = postRequest(kc1, inv, "/%s/invitations".formatted(organization.getId()));
    assertThat(response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));
    String loc = response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // get invitation
    response = getRequest(organization.getId(), "invitations", invitationId);
    assertThat(response.statusCode(), is(Response.Status.OK.getStatusCode()));

    // delete inviter user
    deleteUser(keycloak, REALM, user.getId());

    // get invitation
    response = getRequest(organization.getId(), "invitations", invitationId);
    assertThat(response.statusCode(), is(Response.Status.OK.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization.getId());
  }

  @Test
  void getInvitationsForUserWhenEmailMissing() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitation2
    InvitationRequest inv1 = new InvitationRequest().email("test@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));

    // create a inviter user
    UserRepresentation user = createUserWithCredentials(keycloak, REALM, "user1", "pass");

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // get invitations
    var response = getRequest(kc, "orgs", "me", "invitations");
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void getInvitationsForUserWhenEmailNotValidated() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitation2
    InvitationRequest inv1 = new InvitationRequest().email("test@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", false);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // get invitations
    var response = getRequest(kc, "orgs", "me", "invitations");
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void getInvitationsForUserWhenEmailSimilar() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    var organization2 =
        createOrganization(
            new OrganizationRepresentation().name("example-org2").domains(List.of("example2.com")));

    // create invitation2
    InvitationRequest inv1 = new InvitationRequest().email("+2+test@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));

    InvitationRequest inv2 = new InvitationRequest().email("test+1@phasetwo.io");
    var create2Response = postRequest(inv2, "/%s/invitations".formatted(organization2.getId()));
    assertThat(create2Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // get invitations
    var response = getRequest(kc, "orgs", "me", "invitations");
    assertThat(response.statusCode(), is(Response.Status.OK.getStatusCode()));

    List<UserWithOrgs> invitations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(invitations, notNullValue());
    assertEquals(0, invitations.size());

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());
    deleteOrganization(keycloak, organization2.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void getInvitationsForUser() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    var organization2 =
        createOrganization(
            new OrganizationRepresentation().name("example-org2").domains(List.of("example2.com")));

    // create invitation2
    InvitationRequest inv1 = new InvitationRequest().email("test@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));

    InvitationRequest inv2 = new InvitationRequest().email("test@phasetwo.io");
    var create2Response = postRequest(inv2, "/%s/invitations".formatted(organization2.getId()));
    assertThat(create2Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // get invitations
    var response = getRequest(kc, "orgs", "me", "invitations");
    assertThat(response.statusCode(), is(Response.Status.OK.getStatusCode()));

    List<UserWithOrgs> invitations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(invitations, notNullValue());
    assertEquals(2, invitations.size());

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());
    deleteOrganization(keycloak, organization2.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void acceptInvitationForUser() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = postRequest(kc, null, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.NO_CONTENT.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void acceptInvitationForUserWithoutEmail() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test+2@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user = createUserWithEmail(keycloak, REALM, "user1", "pass", null, true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = postRequest(kc, null, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void acceptInvitationForAnotherUser() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test+2@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = postRequest(kc, null, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void acceptInvitationWhenEmailNotVerified() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test+2@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", false);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = postRequest(kc, null, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void rejectInvitationForUser() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = deleteRequest(kc, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.NO_CONTENT.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void rejectInvitationForAnotherUser() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test+2@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = deleteRequest(kc, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void rejectInvitationForUserWithoutEmail() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test+2@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user = createUserWithEmail(keycloak, REALM, "user1", "pass", null, true);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = deleteRequest(kc, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  @Test
  void rejectInvitationWhenEmailNotVerified() throws IOException {
    // create organizations
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org1").domains(List.of("example1.com")));

    // create invitations
    InvitationRequest inv1 = new InvitationRequest().email("test+2@phasetwo.io");
    var create1Response = postRequest(inv1, "/%s/invitations".formatted(organization1.getId()));
    assertThat(create1Response.statusCode(), is(Response.Status.CREATED.getStatusCode()));
    assertNotNull(create1Response.getHeader("Location"));
    String loc = create1Response.getHeader("Location");
    String invitationId = loc.substring(loc.lastIndexOf("/") + 1);

    // create a inviter user
    UserRepresentation user =
        createUserWithEmail(keycloak, REALM, "user1", "pass", "test@phasetwo.io", false);

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    var kc = getKeycloak(REALM, "test-ui", "user1", "pass");

    // accept invitation
    var response = deleteRequest(kc, String.join("/", "me", "invitations", invitationId));
    assertThat(response.statusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));

    // cleanup

    // remove organization
    deleteOrganization(keycloak, organization1.getId());

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
  }

  public static UserRepresentation createUserWithEmail(
      Keycloak keycloak,
      String realm,
      String username,
      String password,
      String email,
      boolean emailVerified) {
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType(CredentialRepresentation.PASSWORD);
    pass.setValue(password);
    pass.setTemporary(false);
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(username);
    user.setEmail(email);
    user.setEmailVerified(emailVerified);
    user.setCredentials(ImmutableList.of(pass));
    return createUser(keycloak, realm, user);
  }

  @Test
  void testGetUserInvitationsFromMultipleOrganizations() throws IOException {
    // Create multiple organizations
    OrganizationRepresentation org1 =
        createOrganization(
            new OrganizationRepresentation().name("org1").domains(List.of("org1.com")));
    String orgId1 = org1.getId();

    OrganizationRepresentation org2 =
        createOrganization(
            new OrganizationRepresentation().name("org2").domains(List.of("org2.com")));
    String orgId2 = org2.getId();

    OrganizationRepresentation org3 =
        createOrganization(
            new OrganizationRepresentation().name("org3").domains(List.of("org3.com")));
    String orgId3 = org3.getId();

    // Create a test user with email matching the invitations
    UserRepresentation testUser =
        createUserWithCredentials(keycloak, REALM, "testuser", "password", "multiorg@example.com");
    String userId = testUser.getId();

    try {
      // Create invitations in multiple organizations for the same user
      InvitationRequest inv1 =
          new InvitationRequest()
              .email("multiorg@example.com")
              .attribute("org", "org1")
              .attribute("role", "member");

      InvitationRequest inv2 =
          new InvitationRequest()
              .email("multiorg@example.com")
              .attribute("org", "org2")
              .attribute("role", "admin");

      InvitationRequest inv3 =
          new InvitationRequest()
              .email("multiorg@example.com")
              .attribute("org", "org3")
              .attribute("department", "engineering");

      // Create invitations in each organization
      var response1 = postRequest(inv1, orgId1, "invitations");
      assertThat(response1.statusCode(), is(Response.Status.CREATED.getStatusCode()));

      var response2 = postRequest(inv2, orgId2, "invitations");
      assertThat(response2.statusCode(), is(Response.Status.CREATED.getStatusCode()));

      var response3 = postRequest(inv3, orgId3, "invitations");
      assertThat(response3.statusCode(), is(Response.Status.CREATED.getStatusCode()));

      // Create an invitation for a different user in org1 to ensure it's not returned
      UserRepresentation otherUser =
          createUserWithCredentials(keycloak, REALM, "otheruser", "password", "other@example.com");
      InvitationRequest otherInv =
          new InvitationRequest().email("other@example.com").attribute("test", "should-not-appear");

      var otherResponse = postRequest(otherInv, orgId1, "invitations");
      assertThat(otherResponse.statusCode(), is(Response.Status.CREATED.getStatusCode()));

      // Test GET /{userId}/invitations endpoint - should return all invitations for this user
      var response = getRequest(keycloak, "users", userId, "invitations");
      assertThat(response.statusCode(), is(Response.Status.OK.getStatusCode()));

      // Debug: Print the actual response to see what we're getting
      System.out.println("Response status: " + response.statusCode());
      System.out.println("Response body: " + response.getBody().asString());

      List<Invitation> invitations =
          objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
      assertThat(invitations, notNullValue());
      System.out.println("Number of invitations found: " + invitations.size());
      assertThat(invitations.size(), is(3)); // Should have exactly 3 invitations

      // Verify we have invitations from all three organizations
      Set<String> organizationIds =
          invitations.stream()
              .map(Invitation::getOrganizationId)
              .collect(java.util.stream.Collectors.toSet());

      assertThat(organizationIds, hasSize(3));
      assertTrue(organizationIds.contains(orgId1));
      assertTrue(organizationIds.contains(orgId2));
      assertTrue(organizationIds.contains(orgId3));

      // Verify each invitation has correct email and attributes
      for (Invitation invitation : invitations) {
        assertThat(invitation.getEmail(), is("multiorg@example.com"));

        if (invitation.getOrganizationId().equals(orgId1)) {
          assertThat(invitation.getAttributes().get("org").get(0), is("org1"));
          assertThat(invitation.getAttributes().get("role").get(0), is("member"));
        } else if (invitation.getOrganizationId().equals(orgId2)) {
          assertThat(invitation.getAttributes().get("org").get(0), is("org2"));
          assertThat(invitation.getAttributes().get("role").get(0), is("admin"));
        } else if (invitation.getOrganizationId().equals(orgId3)) {
          assertThat(invitation.getAttributes().get("org").get(0), is("org3"));
          assertThat(invitation.getAttributes().get("department").get(0), is("engineering"));
        }
      }

      // Verify that the other user's invitation is not included
      boolean hasOtherUserInvitation =
          invitations.stream()
              .anyMatch(
                  inv ->
                      inv.getAttributes().containsKey("test")
                          && inv.getAttributes().get("test").contains("should-not-appear"));
      assertThat(hasOtherUserInvitation, is(false));

      // Test with a user that has no invitations
      UserRepresentation userWithNoInvites =
          createUserWithCredentials(
              keycloak, REALM, "noinvitesuser", "password", "noinvites@example.com");
      var noInvitesResponse =
          getRequest(keycloak, "users", userWithNoInvites.getId(), "invitations");
      assertThat(noInvitesResponse.statusCode(), is(Response.Status.OK.getStatusCode()));

      List<Invitation> noInvitations =
          objectMapper()
              .readValue(noInvitesResponse.getBody().asString(), new TypeReference<>() {});
      assertThat(noInvitations, notNullValue());
      assertThat(noInvitations.size(), is(0));

      // Clean up additional users
      deleteUser(keycloak, REALM, otherUser.getId());
      deleteUser(keycloak, REALM, userWithNoInvites.getId());

    } finally {
      // Clean up
      deleteUser(keycloak, REALM, userId);
      deleteOrganization(orgId1);
      deleteOrganization(orgId2);
      deleteOrganization(orgId3);
    }
  }
}
