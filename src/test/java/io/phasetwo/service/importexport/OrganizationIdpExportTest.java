package io.phasetwo.service.importexport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableMap;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.LinkIdp;
import java.io.IOException;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;

@JBossLog
public class OrganizationIdpExportTest extends AbstractOrganizationTest {

  @Test
  void testOrganizationLinkIdp() throws IOException {
    // create org
    var org = createDefaultOrg();
    var id = org.getId();

    // create identityProvider
    String alias1 = "linking-provider-1";
    createIdentityProvider(alias1);

    // link it
    linkIdpWithOrg(alias1, org.getId());

    // export
    var export = exportOrgs(keycloak, true);
    assertThat(export.getOrganizations(), hasSize(1));

    // validate
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(org.getName()))
        .findFirst()
        .ifPresent(exportOrg -> assertThat(exportOrg.getIdpLink(), is(alias1)));

    // delete org
    deleteOrganization(id);

    // delete idp
    keycloak.realm(REALM).identityProviders().get(alias1).remove();
  }
}
