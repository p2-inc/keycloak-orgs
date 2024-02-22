package io.phasetwo.service.events;

import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.phasetwo.service.Helpers.clearAdminEvents;
import static io.phasetwo.service.Helpers.getOrganizationEvents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JBossLog
class OrganizationResourceAdminEventsTest extends AbstractOrganizationTest {
    private OrganizationRepresentation organization;

    @Test
    void deleteOrganizationEventsTest() throws Exception {
        //create organization
        var organization = createOrganization(
                new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));

        //delete organization
        deleteOrganization(keycloak, organization.getId());

        //results
        var updateEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("CREATE"))
                .toList();

        assertThat(updateEvents, hasSize(1));

        var deleteEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("DELETE"))
                .toList();

        assertThat(deleteEvents, hasSize(1));
    }

    @Test
    void updateOrganizationEventsTest() throws Exception {
        //create organization
        organization = createOrganization(
                new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));

        // update organization
        organization.url("https://www.example.com/")
                .displayName("Example company")
                .attributes(ImmutableMap.of("foo", List.of("bar")));

        putRequest(organization, organization.getId());

        //results
        var createEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("CREATE"))
                .toList();

        assertThat(createEvents, hasSize(1));

        var updateEvents = getOrganizationEvents(keycloak)
                .filter(adminEventRepresentation -> adminEventRepresentation.getOperationType().equals("UPDATE"))
                .toList();

        assertThat(updateEvents, hasSize(1));
    }

    @AfterEach
    public void afterEach() {
        //verify events consistent
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

        //cleanup - Optional
        Optional.ofNullable(organization).ifPresent(org -> deleteOrganization(keycloak, org.getId()));
    }

    @BeforeEach
    public void beforeEach() {
        clearAdminEvents(keycloak, "master");
    }
}
