package io.phasetwo.service.events;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
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
class OrganizationRolesAdminEventsTest extends AbstractOrganizationTest {
    private OrganizationRepresentation organization;

    @Test
    void updateOrganizationRoleEventsTest() throws IOException {
        // create a role
        String orgRoleName = "eat-apples";
        var organizationRole = createOrgRole(organization.getId(), orgRoleName);

        // update role
        organizationRole.description("Two");
        putRequest(organizationRole, organization.getId(), "roles", orgRoleName);

        //results
        var createEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.ORGANIZATION_ROLE.toString()))
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("CREATE"))
                .toList();

        assertThat(createEvents, hasSize(1));

        var updateEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.ORGANIZATION_ROLE.toString()))
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("UPDATE"))
                .toList();

        assertThat(updateEvents, hasSize(1));

        // delete role
        deleteRequest(organization.getId(), "roles", orgRoleName);
    }

    @Test
    void deleteOrganizationRoleEventsTest() throws IOException {
        // create a role
        String orgRoleName = "eat-apples";
        createOrgRole(organization.getId(), orgRoleName);

        // delete role
        deleteRequest(organization.getId(), "roles", orgRoleName);

        //results
        var createEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.ORGANIZATION_ROLE.toString()))
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("CREATE"))
                .toList();

        assertThat(createEvents, hasSize(1));

        var deleteEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation ->
                        adminEventRepresentation.getResourceType().equals(OrganizationResourceType.ORGANIZATION_ROLE.toString()))
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
