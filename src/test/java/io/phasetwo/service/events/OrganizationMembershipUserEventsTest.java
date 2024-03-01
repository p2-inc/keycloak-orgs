package io.phasetwo.service.events;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.SwitchOrganization;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.phasetwo.service.Helpers.clearEvents;
import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.deleteUser;
import static io.phasetwo.service.Helpers.getEvents;
import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@JBossLog
class OrganizationMembershipUserEventsTest extends AbstractOrganizationTest {
    private OrganizationRepresentation organization;

    @Test
    void organizationMembershipRemovalTest() throws IOException {
        // create a user
        var user = createUser(keycloak, REALM, "johndoe");

        // add membership
        putRequest("foo", organization.getId(), "members", user.getId());

        // delete membership
        deleteRequest(organization.getId(), "members", user.getId());

        //results
        var userEvents = getEvents(keycloak, "master")
                .stream()
                .filter(eventRepresentation -> eventRepresentation.getType().equals("UPDATE_PROFILE"))
                .filter(eventRepresentation -> eventRepresentation.getDetails().containsKey("removed_active_organization_id"))
                .toList();

        assertThat(userEvents, hasSize(1));

        // delete user
        deleteUser(keycloak, REALM, user.getId());
    }

    @Test
    void organizationMembershipSwitchTest() throws IOException {
        // create a user
        var user = createUserWithCredentials(keycloak, REALM, "user", "password");
        // Assign manage-account role
        grantClientRoles("account", user.getId(), "manage-account", "view-profile");

        // add membership
        putRequest("foo", organization.getId(), "members", user.getId());

        //create second organization
        var organization2 = createOrganization(
                new OrganizationRepresentation().name("example-org-2").domains(List.of("example2.com")));

        // add second organization membership
        putRequest("foo", organization2.getId(), "members", user.getId());

        // Client SETUP
        // Create basic front end client to get a proper user access token
        createPublicClient("test-ui");
        var kc = getKeycloak(REALM, "test-ui", "user", "password");

        //assign active organization
        createOrReplaceUserAttribute(kc, "user", ACTIVE_ORGANIZATION, organization.getId());

        // switch to organization2
         var switchToOrganization = new SwitchOrganization().id(organization2.getId());
         putRequest(kc, switchToOrganization, "users", "switch-organization");

        //results
        var userEvents = getEvents(keycloak, "master")
                .stream()
                .filter(eventRepresentation -> eventRepresentation.getType().equals("UPDATE_PROFILE"))
                .filter(eventRepresentation -> eventRepresentation.getDetails().containsKey("new_active_organization_id"))
                .filter(eventRepresentation -> eventRepresentation.getDetails().containsKey("previous_active_organization_id"))
                .toList();

        assertThat(userEvents, hasSize(1));

        // cleanup
        deleteClient("test-ui");
        deleteOrganization(keycloak, organization2.getId());
        deleteUser(keycloak, REALM, user.getId());
    }

    @AfterEach
    public void afterEach() {
        //remove organization
        deleteOrganization(keycloak, organization.getId());
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        clearEvents(keycloak, "master");

        //create organization
        organization = createOrganization(
                new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));
    }
}
