package io.phasetwo.service.importexport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.InvitationRequest;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;

@JBossLog
public class OrganizationInvitationsExportTest extends AbstractOrganizationTest {

  @Test
  void organizationInvitationsExportWithExportMembersAndInvitationsTrueTest() throws IOException {
    // create organization1
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));

    // create role for organization1
    createOrgRole(organization1.getId(), "example-role");
    createOrgRole(organization1.getId(), "example-role2");

    // create organization2
    var organization2 =
        createOrganization(
            new OrganizationRepresentation().name("example-org2").domains(List.of("example2.com")));

    // create role for organization2
    createOrgRole(organization2.getId(), "example2-role");

    // create invitation
    InvitationRequest inv =
        new InvitationRequest()
            .email("johndoe@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty")
            .role("example-role");
    postRequest(inv, organization1.getId(), "invitations");

    InvitationRequest inv2 =
        new InvitationRequest()
            .email("johndoe2@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty")
            .roles(List.of("example-role", "example-role2"));
    postRequest(inv2, organization1.getId(), "invitations");

    InvitationRequest inv3 =
        new InvitationRequest()
            .email("johndoe2@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty")
            .role("example2-role");
    postRequest(inv3, organization2.getId(), "invitations");

    InvitationRequest inv4 =
        new InvitationRequest()
            .email("johndoe3@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .redirectUri("test.com");
    postRequest(inv4, organization2.getId(), "invitations");

    // results
    // export
    var export = exportOrgs(keycloak, true);
    assertThat(export.getOrganizations(), hasSize(2));

    // validate
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization1.getName()))
        .findFirst()
        .ifPresent(
            exportOrg -> validateOrgInvitations(exportOrg.getInvitations(), organization1, REALM));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization2.getName()))
        .findFirst()
        .ifPresent(
            exportOrg -> validateOrgInvitations(exportOrg.getInvitations(), organization2, REALM));

    // remove organization
    deleteOrganization(keycloak, organization1.getId());
    deleteOrganization(keycloak, organization2.getId());
  }

  @Test
  void organizationInvitationsExportWithExportMembersAndInvitationsFalseTest() throws IOException {
    // create organization1
    var organization1 =
        createOrganization(
            new OrganizationRepresentation().name("example-org").domains(List.of("example.com")));

    // create role for organization1
    createOrgRole(organization1.getId(), "example-role");
    createOrgRole(organization1.getId(), "example-role2");

    // create organization2
    var organization2 =
        createOrganization(
            new OrganizationRepresentation().name("example-org2").domains(List.of("example2.com")));

    // create role for organization2
    createOrgRole(organization2.getId(), "example2-role");

    // create invitation
    InvitationRequest inv =
        new InvitationRequest()
            .email("johndoe@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty")
            .role("example-role");
    postRequest(inv, organization1.getId(), "invitations");

    InvitationRequest inv2 =
        new InvitationRequest()
            .email("johndoe2@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty")
            .roles(List.of("example-role", "example-role2"));
    postRequest(inv2, organization1.getId(), "invitations");

    InvitationRequest inv3 =
        new InvitationRequest()
            .email("johndoe2@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty")
            .role("example2-role");
    postRequest(inv3, organization2.getId(), "invitations");

    InvitationRequest inv4 =
        new InvitationRequest()
            .email("johndoe3@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .redirectUri("test.com");
    postRequest(inv4, organization2.getId(), "invitations");

    // results
    // export
    var export = exportOrgs(keycloak, false);
    assertThat(export.getOrganizations(), hasSize(2));

    // validate
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization1.getName()))
        .findFirst()
        .ifPresent(exportOrg -> assertThat(exportOrg.getInvitations(), hasSize(0)));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization2.getName()))
        .findFirst()
        .ifPresent(exportOrg -> assertThat(exportOrg.getInvitations(), hasSize(0)));

    // remove organization
    deleteOrganization(keycloak, organization1.getId());
    deleteOrganization(keycloak, organization2.getId());
  }
}
