package io.phasetwo.service.events;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.resource.OrganizationResourceType;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.phasetwo.service.Helpers.clearAdminEvents;
import static io.phasetwo.service.Helpers.getOrganizationEvents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JBossLog
class InvitationsAdminEventsTest extends AbstractOrganizationTest {
    private OrganizationRepresentation organization;

    @Test
    void createInvitation() throws IOException {
        // create invitation
        InvitationRequest inv = new InvitationRequest()
                .email("johndoe@example.com")
                .attribute("foo", "bar")
                .attribute("foo", "bar2")
                .attribute("humpty", "dumpty");
        var response = postRequest(inv, organization.getId(), "invitations");
        String loc = response.getHeader("Location");
        String inviteId = loc.substring(loc.lastIndexOf("/") + 1);

        //results
        var createEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.INVITATION.toString()))
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("CREATE"))
                .toList();

        assertThat(createEvents, hasSize(1));

        //cleanup
        deleteRequest(organization.getId(), "invitations", inviteId);
    }

    @Test
    void deleteInvitation() throws IOException {
        // create invitation
        InvitationRequest inv = new InvitationRequest()
                .email("johndoe@example.com")
                .attribute("foo", "bar")
                .attribute("foo", "bar2")
                .attribute("humpty", "dumpty");
        var response = postRequest(inv, organization.getId(), "invitations");
        String loc = response.getHeader("Location");
        String inviteId = loc.substring(loc.lastIndexOf("/") + 1);

        //delete invitation
        deleteRequest(organization.getId(), "invitations", inviteId);

        //results
        var createEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.INVITATION.toString()))
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("CREATE"))
                .toList();

        assertThat(createEvents, hasSize(1));

        var deleteEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.INVITATION.toString()))
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("DELETE"))
                .toList();

        assertThat(deleteEvents, hasSize(1));
    }

    @AfterEach
    public void afterEach() {
        getOrganizationEvents(keycloak)
                .forEach(
                        adminEventRepresentation -> {
                            assertNotNull(adminEventRepresentation.getResourcePath());
                            assertNotNull(adminEventRepresentation.getRepresentation());
                            assertNotNull(adminEventRepresentation.getAuthDetails().getClientId());
                            assertNotNull(adminEventRepresentation.getAuthDetails().getIpAddress());
                            assertNotNull(adminEventRepresentation.getAuthDetails().getRealmId());
                            assertNotNull(adminEventRepresentation.getAuthDetails().getUserId());
                        });
        //remove organization
        deleteOrganization(keycloak, organization.getId());
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        clearAdminEvents(keycloak, "master");

        //create organization
        organization = createOrganization(
                new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));
    }
}
