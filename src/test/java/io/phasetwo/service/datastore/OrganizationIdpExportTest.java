package io.phasetwo.service.datastore;

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
    org.keycloak.representations.idm.IdentityProviderRepresentation idp =
        new org.keycloak.representations.idm.IdentityProviderRepresentation();
    idp.setAlias(alias1);
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("useJwksUrl", "true")
            .put("syncMode", "FORCE")
            .put("authorizationUrl", "https://foo.com")
            .put("hideOnLoginPage", "")
            .put("loginHint", "")
            .put("uiLocales", "")
            .put("backchannelSupported", "")
            .put("disableUserInfo", "")
            .put("acceptsPromptNoneForwardFromClient", "")
            .put("validateSignature", "")
            .put("pkceEnabled", "")
            .put("tokenUrl", "https://foo.com")
            .put("clientAuthMethod", "client_secret_post")
            .put("clientId", "aabbcc")
            .put("clientSecret", "112233")
            .build());
    keycloak.realm(REALM).identityProviders().create(idp);

    // link it
    LinkIdp link = new LinkIdp();
    link.setAlias(alias1);
    link.setSyncMode("IMPORT");

    postRequest(link, org.getId(), "idps", "link");

    // export
    var export = export(keycloak);
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
