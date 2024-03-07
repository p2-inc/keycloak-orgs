package io.phasetwo.service.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@JBossLog
public class KeycloakOrganizationExportTest extends AbstractOrganizationTest {
  @Test
  void testOrganizationExport() throws Exception {
    // create organization1
    var rep1 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example1")
                .domains(List.of("example1.com"))
                .url("www.example1.com")
                .displayName("Example")
                .attributes(Map.of("attr", List.of("value"))));

    // create organization2
    var rep2 =
        createOrganization(
            new OrganizationRepresentation().name("example2").domains(List.of("example2.com")));

    // create organization3
    var rep3 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example3")
                .attributes(Map.of("attr", List.of("value"))));

    // create organization4
    var rep4 =
        createOrganization(
            new OrganizationRepresentation().name("example4").displayName("example4"));

    // create organization5
    var rep5 =
        createOrganization(new OrganizationRepresentation().name("example5").url("example5.com"));

    // export
    var export = export(keycloak);
    assertThat(export.getOrganizations(), hasSize(5));

    // validate
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(rep1.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrg(exportOrg, rep1));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(rep2.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrg(exportOrg, rep2));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(rep3.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrg(exportOrg, rep3));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(rep4.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrg(exportOrg, rep4));

    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(rep5.getName()))
        .findFirst()
        .ifPresent(exportOrg -> validateOrg(exportOrg, rep5));

    // delete
    deleteOrganization(rep1.getId());
    deleteOrganization(rep2.getId());
    deleteOrganization(rep3.getId());
    deleteOrganization(rep4.getId());
    deleteOrganization(rep5.getId());
  }

  private static void validateOrg(
      io.phasetwo.service.datastore.representation.OrganizationRepresentation
          exportOrganizationReprezentation,
      OrganizationRepresentation rep) {
    assertThat(
        exportOrganizationReprezentation.getOrganization().getName(), Matchers.is(rep.getName()));

    if (rep.getDomains().isEmpty()) {
      assertThat(exportOrganizationReprezentation.getOrganization().getDomains(), hasSize(0));
    } else {
      assertThat(
          exportOrganizationReprezentation.getOrganization().getDomains(),
          contains(rep.getDomains().toArray()));
    }
    assertThat(
        exportOrganizationReprezentation.getOrganization().getUrl(), Matchers.is(rep.getUrl()));
    assertThat(
        exportOrganizationReprezentation.getOrganization().getDisplayName(),
        Matchers.is(rep.getDisplayName()));
    if (rep.getAttributes().isEmpty()) {
      assertThat(
          exportOrganizationReprezentation.getOrganization().getAttributes().entrySet(),
          hasSize(0));
    } else {
      assertThat(
          exportOrganizationReprezentation.getOrganization().getAttributes().entrySet(),
          contains(rep.getAttributes().entrySet().toArray()));
    }
  }
}
