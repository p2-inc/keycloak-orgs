package io.phasetwo.service.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;

@JBossLog
public class KeycloakOrganizationRealmImportExportTest extends AbstractOrganizationTest {
  @Test
  void testOrganizationExport() throws Exception {
    createOrganization(
        new OrganizationRepresentation().name("example").domains(List.of("example.com")));

    var export = export(keycloak);
    assertThat(export.getOrganizations(), hasSize(1));
  }
}
