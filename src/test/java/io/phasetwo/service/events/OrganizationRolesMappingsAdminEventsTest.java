package io.phasetwo.service.events;

import static io.phasetwo.service.Helpers.clearAdminEvents;
import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.deleteUser;
import static io.phasetwo.service.Helpers.getOrganizationEvents;
import static io.phasetwo.service.resource.OrganizationAdminAuth.ORG_ROLE_MANAGE_ORGANIZATION;
import static io.phasetwo.service.resource.OrganizationAdminAuth.ORG_ROLE_VIEW_ORGANIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.OrganizationRole;
import io.phasetwo.service.resource.OrganizationResourceType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
class OrganizationRolesMappingsAdminEventsTest extends AbstractOrganizationTest {
  private OrganizationRepresentation organization;
  private UserRepresentation user;

  @Test
  void removeOrganizationRoleMembershipMappingEventsTest() throws IOException {

    // create role
    createOrgRole(organization.getId(), "test-role");

    // add membership
    grantUserRole(organization.getId(), "test-role", user.getId());

    // remove membership
    revokeUserRole(organization.getId(), "test-role", user.getId());

    // results
    var createEvents =
        getOrganizationEvents(keycloak)
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation
                        .getResourceType()
                        .equals(OrganizationResourceType.ORGANIZATION_ROLE_MAPPING.toString()))
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
                        .equals(OrganizationResourceType.ORGANIZATION_ROLE_MAPPING.toString()))
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation.getOperationType().equals("DELETE"))
            .toList();

    assertThat(deleteEvents, hasSize(1));

    // remove role
    deleteRequest(organization.getId(), "roles", "test-role");
  }

  @Test
  void bulkRevokeOrganizationRoleMembershipMappingEventsTest() throws IOException {
    // add role membership
    var url =
        getAuthUrl()
            + "/realms/master/users/"
            + user.getId()
            + "/orgs/"
            + organization.getId()
            + "/roles";
    var roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name(ORG_ROLE_VIEW_ORGANIZATION));
            add(new OrganizationRole().name(ORG_ROLE_MANAGE_ORGANIZATION));
          }
        };

    putRequest(roleList, url);

    // remove role membership
    var revokedRoleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name(ORG_ROLE_VIEW_ORGANIZATION));
          }
        };
    patchRequest(revokedRoleList, url);

    // results
    var createEvents =
        getOrganizationEvents(keycloak)
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation
                        .getResourceType()
                        .equals(OrganizationResourceType.ORGANIZATION_ROLE_MAPPING.toString()))
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation.getOperationType().equals("CREATE"))
            .toList();

    assertThat(createEvents, hasSize(2));

    var deleteEvents =
        getOrganizationEvents(keycloak)
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation
                        .getResourceType()
                        .equals(OrganizationResourceType.ORGANIZATION_ROLE_MAPPING.toString()))
            .filter(
                adminEventRepresentation ->
                    adminEventRepresentation.getOperationType().equals("DELETE"))
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

    // delete user
    deleteUser(keycloak, REALM, user.getId());
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

    // create a user
    user = createUser(keycloak, REALM, "johndoe");

    // add membership
    putRequest("foo", organization.getId(), "members", user.getId());
  }
}
