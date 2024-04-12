package io.phasetwo.service.importexport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.importexport.representation.OrganizationRoleRepresentation;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@JBossLog
public class OrganizationRoleExportTest extends AbstractOrganizationTest {
  @Test
  void testOrganizationRoleExport() throws Exception {
    // create organization1
    var rep1 = createOrganization(new OrganizationRepresentation().name("organization1"));

    // create a role
    String orgRoleName1 = "eat-apples";
    createOrgRole(rep1.getId(), orgRoleName1);

    // create a role
    String orgRoleName2 = "eat-cherry";
    createOrgRole(rep1.getId(), orgRoleName2);

    // create organization2
    var rep2 = createOrganization(new OrganizationRepresentation().name("organization2"));

    // create a role
    String orgRoleName3 = "role2";
    createOrgRole(rep2.getId(), orgRoleName3);

    // export
    var export = exportOrgs(keycloak, true);
    assertThat(export.getOrganizations(), hasSize(2));

    // validate
    var rolesOrg1 =
        export.getOrganizations().stream()
            .filter(
                organizationRepresentation ->
                    organizationRepresentation.getOrganization().getName().equals(rep1.getName()))
            .flatMap(exportOrg -> exportOrg.getRoles().stream())
            .map(OrganizationRoleRepresentation::getName)
            .toList();
    Assertions.assertTrue(rolesOrg1.contains(orgRoleName1));
    Assertions.assertTrue(rolesOrg1.contains(orgRoleName2));

    var rolesOrg2 =
        export.getOrganizations().stream()
            .filter(
                organizationRepresentation ->
                    organizationRepresentation.getOrganization().getName().equals(rep2.getName()))
            .flatMap(exportOrg -> exportOrg.getRoles().stream())
            .map(OrganizationRoleRepresentation::getName)
            .toList();
    Assertions.assertTrue(rolesOrg2.contains(orgRoleName3));

    // delete
    deleteOrganization(rep1.getId());
    deleteOrganization(rep2.getId());
  }
}
