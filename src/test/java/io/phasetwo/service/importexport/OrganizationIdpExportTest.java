package io.phasetwo.service.importexport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.representation.OrganizationsConfig;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

  @Test
  void testOrganizationChangeLink() throws IOException {
    // create organization
    var organization1 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example-org")
                .domains(List.of("example.com", "test.org")));
    // create organization
    var organization2 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example-org2")
                .domains(List.of("example2.com", "test2.org")));

    // create identity provider
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

    // link org1
    LinkIdp link1 = new LinkIdp();
    link1.setAlias(alias1);
    link1.setSyncMode("IMPORT");
    var responseOrg1Link = postRequest(link1, organization1.getId(), "idps", "link");
    assertThat(
        responseOrg1Link.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // link org2
    var link2 = new LinkIdp();
    link2.setAlias(alias1);
    link2.setSyncMode("IMPORT");
    var responseOrg2Link = postRequest(link2, organization2.getId(), "idps", "link");
    assertThat(
        responseOrg2Link.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // export
    var export = exportOrgs(keycloak, true);
    assertThat(export.getOrganizations(), hasSize(2));

    // validate org1
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization1.getName()))
        .findFirst()
        .ifPresent(exportOrg -> Assertions.assertNull(exportOrg.getIdpLink()));

    // validate org2
    export.getOrganizations().stream()
        .filter(exportOrg -> exportOrg.getOrganization().getName().equals(organization2.getName()))
        .findFirst()
        .ifPresent(exportOrg -> assertThat(exportOrg.getIdpLink(), is(alias1)));

    // delete org1
    deleteOrganization(organization1.getId());

    // delete org2
    deleteOrganization(organization2.getId());

    // delete idp
    keycloak.realm(REALM).identityProviders().get(alias1).remove();
  }

  @BeforeEach
  public void beforeEach() throws JsonProcessingException {
    // add shared idp master config
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setSharedIdps(false);
    var responseOrgsConfig = putRequest(orgConfig, url);
    assertThat(
        responseOrgsConfig.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
  }

  @AfterEach
  public void afterEach() throws JsonProcessingException {
    // remove shared idp master config
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setSharedIdps(false);
    var responseOrgsConfig = putRequest(orgConfig, url);
    assertThat(
        responseOrgsConfig.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
  }
}
