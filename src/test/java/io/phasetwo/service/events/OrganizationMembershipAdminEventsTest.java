package io.phasetwo.service.events;

import static io.phasetwo.service.Helpers.clearAdminEvents;
import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.deleteUser;
import static io.phasetwo.service.Helpers.getOrganizationEvents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.resource.OrganizationResourceType;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@JBossLog
class OrganizationMembershipAdminEventsTest extends AbstractOrganizationTest {
  private OrganizationRepresentation organization;

  @Test
  void organizationMembershipEventsTest() throws IOException {
    // create a user
    var user = createUser(keycloak, REALM, "johndoe");

    // add membership
    putRequest("foo", organization.getId(), "members", user.getId());

    // delete membership
    deleteRequest(organization.getId(), "members", user.getId());

    // results
    var createEvents =
        getOrganizationEvents(keycloak)
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation
                        .getResourceType()
                        .equals(OrganizationResourceType.ORGANIZATION_MEMBERSHIP.toString()))
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation.getOperationType().equals("CREATE"))
            .toList();

    assertThat(createEvents, hasSize(1));

    var deleteEvents =
        getOrganizationEvents(keycloak)
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation
                        .getResourceType()
                        .equals(OrganizationResourceType.ORGANIZATION_MEMBERSHIP.toString()))
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation.getOperationType().equals("DELETE"))
            .toList();

    assertThat(deleteEvents, hasSize(1));

    // delete user
    deleteUser(keycloak, REALM, user.getId());
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

    // remove organization
    deleteOrganization(keycloak, organization.getId());
  }

  @BeforeEach
  public void beforeEach() throws IOException {
    clearAdminEvents(keycloak, "master");

    // create organization
    organization =
        createOrganization(
            new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));
  }
}
