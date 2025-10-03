package io.phasetwo.service.globalconfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.IdentityProviderMapperRepresentation;
import io.phasetwo.client.openapi.model.IdentityProviderRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.representation.OrganizationsConfig;
import io.restassured.response.Response;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@JBossLog
public class MultiIdpsEnabledTest extends AbstractOrganizationTest {

  @BeforeEach
  public void beforeEach() throws JsonProcessingException {
    // add shared idp master config
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setMultipleIdps(true);
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
    orgConfig.setMultipleIdps(false);
    var responseOrgsConfig = putRequest(orgConfig, url);
    assertThat(
        responseOrgsConfig.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
  }

  @Test
  void testAddGetDeleteMultiIdps() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    String alias1 = "vendor-protocol-1";
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias(alias1);
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
            new ImmutableMap.Builder<String, Object>()
                    .put("useJwksUrl", "true")
                    .put("syncMode", "IMPORT")
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

    // create idp
    Response response = postRequest(idp, id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // get idps
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps =
            objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));

    IdentityProviderRepresentation representation = idps.get(0);
    assertThat(representation.getEnabled(), is(true));
    assertThat(representation.getAlias(), is(alias1));
    assertThat(representation.getProviderId(), is("oidc"));

    // create idp
    String alias2 = "vendor-protocol-2";
    representation.setAlias(alias2);
    representation.setInternalId(null);
    response = postRequest(representation, id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // get idps
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    idps = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(2));
    for (IdentityProviderRepresentation i : idps) {
      Assertions.assertTrue(i.getEnabled());
    }

    // get mappers for idp2
    var mapperResponse = getRequest(id, "idps", alias2, "mappers");
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderMapperRepresentation> mappers =
            objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, empty());

    // add a mapper to the idp
    // {"identityProviderAlias":"oidc","config":
    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setIdentityProviderAlias(alias2);
    mapper.setName("name");
    mapper.setIdentityProviderMapper("oidc-user-attribute-idp-mapper");
    mapper.setConfig(
            new ImmutableMap.Builder<String, Object>()
                    .put("syncMode", "INHERIT")
                    .put("user.attribute", "name")
                    .put("claim", "name")
                    .build());
    mapperResponse = postRequest(mapper, id, "idps", alias2, "mappers");
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // get mappers for idp2

    mapperResponse = getRequest(id, "idps", alias2, "mappers");
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    mappers = objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, hasSize(1));
    String mapperId = mappers.get(0).getId();
    assertThat(mapperId, notNullValue());

    // get single mapper for idp2
    mapperResponse = getRequest(id, "idps", alias2, "mappers", mapperId);
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    mapper = objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mapper, notNullValue());
    assertThat(mapper.getName(), is("name"));
    assertThat(mapper.getIdentityProviderAlias(), is(alias2));
    assertThat(mapper.getIdentityProviderMapper(), is("oidc-user-attribute-idp-mapper"));

    // update mapper for idp2
    mapper.setConfig(
            new ImmutableMap.Builder<String, Object>()
                    .put("syncMode", "INHERIT")
                    .put("user.attribute", "lastName")
                    .put("claim", "familyName")
                    .build());

    mapperResponse = putRequest(mapper, id, "idps", alias2, "mappers", mapperId);
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

    // get single mapper for idp2
    mapperResponse = getRequest(id, "idps", alias2, "mappers", mapperId);
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    mapper = objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mapper, notNullValue());
    assertThat(mapper.getConfig().get("user.attribute"), is("lastName"));
    assertThat(mapper.getConfig().get("claim"), is("familyName"));

    // delete mappers for idp2
    mapperResponse = deleteRequest(id, "idps", alias2, "mappers", mapperId);
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

    // get mappers for idp2
    mapperResponse = getRequest(id, "idps", alias2, "mappers");
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    mappers = objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, empty());

    // get mappers for idp2
    mapperResponse = getRequest(id, "idps", alias2, "mappers");
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    mappers = objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, empty());

    // get mappers for idp1

    mapperResponse = getRequest(id, "idps", alias1, "mappers");
    assertThat(mapperResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    mappers = objectMapper().readValue(mapperResponse.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, hasSize(0));

    // delete idps
    response = deleteRequest(id, "idps", alias1);
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));
    response = deleteRequest(id, "idps", alias2);
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

    // get idps
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    idps = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, empty());

    // delete org
    deleteOrganization(id);
  }


  @Test
  void testLinkMultiIdps() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

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
    Response response = postRequest(link, org.getId(), "idps", "link");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // get it
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps =
            objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));

    IdentityProviderRepresentation representation = idps.get(0);
    assertThat(representation.getEnabled(), is(true));
    assertThat(representation.getAlias(), is(alias1));
    assertThat(representation.getProviderId(), is("oidc"));
    assertThat(representation.getConfig().get("syncMode"), is("IMPORT"));


    // create another IDP
    String alias2 = "linking-provider-2";
    org.keycloak.representations.idm.IdentityProviderRepresentation idp2 =
            new org.keycloak.representations.idm.IdentityProviderRepresentation();
    idp2.setAlias(alias2);
    idp2.setProviderId("oidc");
    idp2.setEnabled(true);
    idp2.setFirstBrokerLoginFlowAlias("first broker login");
    idp2.setConfig(
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
    keycloak.realm(REALM).identityProviders().create(idp2);

    // link it
    LinkIdp link2 = new LinkIdp();
    link2.setAlias(alias2);
    link2.setSyncMode("IMPORT");
    Response response2 = postRequest(link2, org.getId(), "idps", "link");
    assertThat(response2.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

    // get it
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps2 =
            objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps2, notNullValue());
    assertThat(idps2, hasSize(2));

    IdentityProviderRepresentation representation1 = idps2.get(0);
    assertThat(representation1.getEnabled(), is(true));
    assertThat(representation1.getAlias(), is(alias1));
    assertThat(representation1.getProviderId(), is("oidc"));
    assertThat(representation1.getConfig().get("syncMode"), is("IMPORT"));

    IdentityProviderRepresentation representation2 = idps2.get(1);
    assertThat(representation2.getEnabled(), is(true));
    assertThat(representation2.getAlias(), is(alias2));
    assertThat(representation2.getProviderId(), is("oidc"));
    assertThat(representation2.getConfig().get("syncMode"), is("IMPORT"));

    // unlink alias1
    response = postRequest("foo", org.getId(), "idps", representation1.getAlias(), "unlink");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

    // check it alias1
    response = getRequest(id, "idps", representation1.getAlias());
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode()));

    // get it alias1
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps3 =
            objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps3, notNullValue());
    assertThat(idps3, hasSize(1));

    // unlink alias2
    response = postRequest("foo", org.getId(), "idps", representation2.getAlias(), "unlink");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

    // check it alias2
    response = getRequest(id, "idps", representation2.getAlias());
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode()));

    // get it alias2
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps4 =
            objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps4, notNullValue());
    assertThat(idps4, hasSize(0));

    // delete org
    deleteOrganization(id);

    // delete idp
    keycloak.realm(REALM).identityProviders().get(alias1).remove();
    keycloak.realm(REALM).identityProviders().get(alias2).remove();
  }
}
