package io.phasetwo.service.importexport;

import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.deleteUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import java.io.IOException;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;

@JBossLog
public class OrganizationMembersExportTest extends AbstractOrganizationTest {

  @Test
  void organizationMembersExportWithExportMembersAndInvitationsTrueTest() throws IOException {
    // create users
    var user1 = createUser(keycloak, REALM, "johndoe1");
    var user2 = createUser(keycloak, REALM, "johndoe2");
    var user3 = createUser(keycloak, REALM, "johndoe3");

    // create organization1
    var organization1 =
        createOrganization(new OrganizationRepresentation().name("example").displayName("example"));

    // create role for organization1
    createOrgRole(organization1.getId(), "example-role");
    createOrgRole(organization1.getId(), "example-role2");

    // create organization2
    var organization2 =
        createOrganization(
            new OrganizationRepresentation().name("example2").displayName("example2"));

    // create role for organization2
    createOrgRole(organization2.getId(), "example2-role");

    // add membership
    putRequest("foo", organization1.getId(), "members", user1.getId());

    putRequest("foo", organization1.getId(), "members", user2.getId());

    putRequest("foo", organization2.getId(), "members", user2.getId());

    putRequest("foo", organization2.getId(), "members", user3.getId());

    // add role to users for org1
    grantUserRole(organization1.getId(), "example-role", user1.getId());
    grantUserRole(organization1.getId(), "example-role2", user2.getId());
    grantUserRole(organization1.getId(), "example-role", user2.getId());

    // add role to users for org2
    grantUserRole(organization2.getId(), "example2-role", user2.getId());

    // results
    // export
    var export = exportOrgs(keycloak, true);
    assertThat(export.getOrganizations(), hasSize(2));

    // validate
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization1.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrgMembers(exportOrg.getMembers(), organization1, REALM));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization2.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrgMembers(exportOrg.getMembers(), organization2, REALM));

    // delete users
    deleteUser(keycloak, REALM, user1.getId());
    deleteUser(keycloak, REALM, user2.getId());
    deleteUser(keycloak, REALM, user3.getId());

    // delete organizations
    deleteOrganization(organization1.getId());
    deleteOrganization(organization2.getId());
  }

  @Test
  void organizationMembersExportWithExportMembersAndInvitationsFalseTest() throws IOException {
    // create users
    var user1 = createUser(keycloak, REALM, "johndoe1");
    var user2 = createUser(keycloak, REALM, "johndoe2");
    var user3 = createUser(keycloak, REALM, "johndoe3");

    // create organization1
    var organization1 =
        createOrganization(new OrganizationRepresentation().name("example").displayName("example"));

    // create role for organization1
    createOrgRole(organization1.getId(), "example-role");
    createOrgRole(organization1.getId(), "example-role2");

    // create organization2
    var organization2 =
        createOrganization(
            new OrganizationRepresentation().name("example2").displayName("example2"));

    // create role for organization2
    createOrgRole(organization2.getId(), "example2-role");

    // add membership
    putRequest("foo", organization1.getId(), "members", user1.getId());

    putRequest("foo", organization1.getId(), "members", user2.getId());

    putRequest("foo", organization2.getId(), "members", user2.getId());

    putRequest("foo", organization2.getId(), "members", user3.getId());

    // add role to users for org1
    grantUserRole(organization1.getId(), "example-role", user1.getId());
    grantUserRole(organization1.getId(), "example-role2", user2.getId());
    grantUserRole(organization1.getId(), "example-role", user2.getId());

    // add role to users for org2
    grantUserRole(organization2.getId(), "example2-role", user2.getId());

    // results
    // export
    var export = exportOrgs(keycloak, false);
    assertThat(export.getOrganizations(), hasSize(2));

    // validate
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization1.getName()))
        .findFirst()
        .ifPresent(exportOrg -> assertThat(exportOrg.getMembers(), hasSize(0)));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization2.getName()))
        .findFirst()
        .ifPresent(exportOrg -> assertThat(exportOrg.getMembers(), hasSize(0)));

    // delete users
    deleteUser(keycloak, REALM, user1.getId());
    deleteUser(keycloak, REALM, user2.getId());
    deleteUser(keycloak, REALM, user3.getId());

    // delete organizations
    deleteOrganization(organization1.getId());
    deleteOrganization(organization2.getId());
  }
}
