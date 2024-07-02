package io.phasetwo.service.globalconfig;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.IdentityProviderRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.Orgs;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.representation.OrganizationsConfig;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.Constants;

@JBossLog
public class SharedIdpsDisabledTest extends AbstractOrganizationTest {

  @Test
  void testOrganizationMultiLink() throws IOException {
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

    // get IDP for org1
    var org1GetResponse = getRequest(organization1.getId(), "idps");
    assertThat(
        org1GetResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps =
        objectMapper().readValue(org1GetResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(0));

    // get IDP for org2
    var org2GetResponse = getRequest(organization2.getId(), "idps");
    assertThat(
        org2GetResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps2 =
        objectMapper().readValue(org2GetResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(idps2, notNullValue());
    assertThat(idps2, hasSize(1));

    IdentityProviderRepresentation representation2 = idps2.get(0);
    assertThat(representation2.getEnabled(), is(true));
    assertThat(representation2.getAlias(), is(alias1));
    assertThat(representation2.getProviderId(), is("oidc"));
    assertThat(representation2.getConfig().get("syncMode"), is("IMPORT"));

    // check if there is 1 organization linked to the IDP
    var discoveredOrg2Config =
        representation2.getConfig().get(Orgs.ORG_OWNER_CONFIG_KEY).toString();
    var discoveredOrg2 = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(discoveredOrg2Config));
    assertThat(discoveredOrg2.size(), is(1));

    var idpId = idp.getAlias();

    // unlink org1
    var unlinkOrg1Response = postRequest("foo", organization1.getId(), "idps", idpId, "unlink");
    assertThat(unlinkOrg1Response.getStatusCode(), is(Response.Status.NOT_FOUND.getStatusCode()));

    // check if idp is still available for org1
    var org1IdpResponse = getRequest(organization1.getId(), "idps", idpId);
    assertThat(
        org1IdpResponse.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode()));

    // unlink org2
    var unlinkOrg2Response = postRequest("foo", organization2.getId(), "idps", idpId, "unlink");
    assertThat(
        unlinkOrg2Response.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

    // check if idp is still available for org2
    var org2IdpResponse = getRequest(organization1.getId(), "idps", idpId);
    assertThat(
        org2IdpResponse.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode()));

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
