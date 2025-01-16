package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.deleteUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.InvitationRequest;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

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
}
