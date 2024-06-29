package io.phasetwo.service.sharedidps;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.models.Constants;

@JBossLog
public class SharedIdpsEnabledTest extends AbstractOrganizationTest {

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
        link1.setShared(true);
        var responseOrg1Link = postRequest(link1, organization1.getId(), "idps", "link");
        assertThat(
                responseOrg1Link.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

        // link org2
        var link2 = new LinkIdp();
        link2.setAlias(alias1);
        link2.setSyncMode("IMPORT");
        link2.setShared(true);
        var responseOrg2Link = postRequest(link2, organization2.getId(), "idps", "link");
        assertThat(
                responseOrg2Link.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

        // get IDP for org1
        var org1GetResponse = getRequest(organization1.getId(), "idps");
        assertThat(
                org1GetResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
        List<IdentityProviderRepresentation> idps =
                objectMapper().readValue(org1GetResponse.getBody().asString(), new TypeReference<>() {
                });
        assertThat(idps, notNullValue());
        assertThat(idps, hasSize(1));

        IdentityProviderRepresentation representation1 = idps.get(0);
        assertThat(representation1.getEnabled(), is(true));
        assertThat(representation1.getAlias(), is(alias1));
        assertThat(representation1.getProviderId(), is("oidc"));
        assertThat(representation1.getConfig().get("syncMode"), is("IMPORT"));
        assertThat(representation1.getConfig().get(Orgs.ORG_SHARED_IDP_KEY), is("true"));

        // check if there are 2 organizations linked to the IDP
        var discoveredOrg1Config =
                representation1.getConfig().get(Orgs.ORG_OWNER_CONFIG_KEY).toString();
        var discoveredOrg1 = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(discoveredOrg1Config));
        assertThat(discoveredOrg1.size(), is(2));

        // get IDP for org2
        var org2GetResponse = getRequest(organization2.getId(), "idps");
        assertThat(
                org2GetResponse.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
        List<IdentityProviderRepresentation> idps2 =
                objectMapper().readValue(org2GetResponse.getBody().asString(), new TypeReference<>() {
                });
        assertThat(idps2, notNullValue());
        assertThat(idps2, hasSize(1));

        IdentityProviderRepresentation representation2 = idps2.get(0);
        assertThat(representation2.getEnabled(), is(true));
        assertThat(representation2.getAlias(), is(alias1));
        assertThat(representation2.getProviderId(), is("oidc"));
        assertThat(representation2.getConfig().get("syncMode"), is("IMPORT"));
        assertThat(representation2.getConfig().get(Orgs.ORG_SHARED_IDP_KEY), is("true"));

        // check if there are 2 organizations linked to the IDP
        var discoveredOrg2Config =
                representation2.getConfig().get(Orgs.ORG_OWNER_CONFIG_KEY).toString();
        var discoveredOrg2 = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(discoveredOrg2Config));
        assertThat(discoveredOrg2.size(), is(2));

        var idpId = idp.getAlias();

        // unlink org1
        var unlinkOrg1Response = postRequest("foo", organization1.getId(), "idps", idpId, "unlink");
        assertThat(
                unlinkOrg1Response.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

        // get IDP for org1. It should no longer be linked
        var org1IdpResponse = getRequest(organization1.getId(), "idps", idpId);
        assertThat(
                org1IdpResponse.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode()));

        // get IDP for org2. Is should still be linked
        var org2GetIdpResponse = getRequest(organization2.getId(), "idps");
        assertThat(
                org2GetIdpResponse.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
        List<IdentityProviderRepresentation> unlinkedOrg1IdpRepresentation =
                objectMapper().readValue(org2GetIdpResponse.getBody().asString(), new TypeReference<>() {
                });
        assertThat(unlinkedOrg1IdpRepresentation, notNullValue());

        // check if there is 1 organization still linked to the IDP
        var discoveredOrgConfig =
                unlinkedOrg1IdpRepresentation.get(0).getConfig().get(Orgs.ORG_OWNER_CONFIG_KEY).toString();
        var discoveredOrg = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(discoveredOrgConfig));
        assertThat(discoveredOrg.size(), is(1));
        assertThat(discoveredOrg.get(0), is(organization2.getId()));

        // unlink org2
        var unlinkOrg2Response = postRequest("foo", organization2.getId(), "idps", idpId, "unlink");
        assertThat(
                unlinkOrg2Response.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

        // check if IDP is still linked to org2
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

    @Test
    void testIdpLinkUpdate() throws IOException {
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
        link1.setShared(true);
        var responseOrg1Link = postRequest(link1, organization1.getId(), "idps", "link");
        assertThat(
                responseOrg1Link.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

        // link org2
        var link2 = new LinkIdp();
        link2.setAlias(alias1);
        link2.setSyncMode("IMPORT");
        link2.setShared(true);
        var responseOrg2Link = postRequest(link2, organization2.getId(), "idps", "link");
        assertThat(
                responseOrg2Link.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

        var idpId = idp.getAlias();

        // update org1 IDP
        var updateIdpResponse = putRequest(idp, organization1.getId(), "idps", idpId);
        assertThat(
                updateIdpResponse.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));

        // get IDP for org1. Is should still have 2 organizations linked linked
        var org1GetIdpResponse = getRequest(organization2.getId(), "idps");

        List<IdentityProviderRepresentation> org1Idp =
                objectMapper().readValue(org1GetIdpResponse.getBody().asString(), new TypeReference<>() {
                });

        assertThat(org1Idp, notNullValue());
        var representation = org1Idp.get(0);
        assertThat(representation.getConfig().get(Orgs.ORG_SHARED_IDP_KEY), is("true"));
        // check if there are 2 organizations linked to the IDP
        var discoveredOrg1Config =
                representation.getConfig().get(Orgs.ORG_OWNER_CONFIG_KEY).toString();
        var discoveredOrg1 = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(discoveredOrg1Config));
        assertThat(discoveredOrg1.size(), is(2));


        // delete org1
        deleteOrganization(organization1.getId());

        // delete org2
        deleteOrganization(organization2.getId());

        // delete idp
        keycloak.realm(REALM).identityProviders().get(alias1).remove();
    }


    @Test
    void testOrganizationRemoveIdpLink() throws IOException {
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
        link1.setShared(true);
        var responseOrg1Link = postRequest(link1, organization1.getId(), "idps", "link");
        assertThat(
                responseOrg1Link.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

        // link org2
        var link2 = new LinkIdp();
        link2.setAlias(alias1);
        link2.setSyncMode("IMPORT");
        link2.setShared(true);
        var responseOrg2Link = postRequest(link2, organization2.getId(), "idps", "link");
        assertThat(
                responseOrg2Link.getStatusCode(),
                is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));

        // delete org1
        deleteOrganization(organization1.getId());

        // get IDP for org2. It should still have 1 organization linked
        var org2GetIdpResponse = getRequest(organization2.getId(), "idps");

        List<IdentityProviderRepresentation> org2Idp =
                objectMapper().readValue(org2GetIdpResponse.getBody().asString(), new TypeReference<>() {
                });

        assertThat(org2Idp, notNullValue());
        var representation = org2Idp.get(0);
        assertThat(representation.getConfig().get(Orgs.ORG_SHARED_IDP_KEY), is("true"));

        var discoveredOrg2Config =
                representation.getConfig().get(Orgs.ORG_OWNER_CONFIG_KEY).toString();
        var discoveredOrg1 = Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(discoveredOrg2Config));
        assertThat(discoveredOrg1.size(), is(1));


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
        orgConfig.setSharedIdps(true);
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
