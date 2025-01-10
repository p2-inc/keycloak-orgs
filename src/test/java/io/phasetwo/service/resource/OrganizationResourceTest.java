package io.phasetwo.service.resource;

import static com.google.common.collect.Lists.newArrayList;
import static io.phasetwo.service.Helpers.addEventListener;
import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.createWebhook;
import static io.phasetwo.service.Helpers.deleteUser;
import static io.phasetwo.service.Helpers.deleteWebhook;
import static io.phasetwo.service.Helpers.enableEvents;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Helpers.removeEventListener;
import static io.phasetwo.service.Orgs.ACTIVE_ORGANIZATION;
import static io.phasetwo.service.protocol.oidc.mappers.ActiveOrganizationMapper.INCLUDED_ORGANIZATION_PROPERTIES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.xgp.http.server.Server;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.IdentityProviderMapperRepresentation;
import io.phasetwo.client.openapi.model.IdentityProviderRepresentation;
import io.phasetwo.client.openapi.model.InvitationRequestRepresentation;
import io.phasetwo.client.openapi.model.OrganizationDomainRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.client.openapi.model.PortalLinkRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.LegacySimpleHttp;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.representation.OrganizationRole;
import io.phasetwo.service.representation.SwitchOrganization;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

@JBossLog
class OrganizationResourceTest extends AbstractOrganizationTest {

  private static String ACTIVE_ORG_CLAIM = "active_organization";
  private static String ACCESS_TOKEN = "access_token";

  @Test
  void testAddGetUpdateDeleteOrg() throws Exception {
    // get single
    OrganizationRepresentation rep =
        createOrganization(
            new OrganizationRepresentation().name("example").domains(List.of("example.com")));
    String id = rep.getId();

    assertThat(rep, notNullValue());
    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getDisplayName(), CoreMatchers.nullValue());
    assertThat(rep.getUrl(), CoreMatchers.nullValue());
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // get list
    List<OrganizationRepresentation> organizations = getListOfOrganizations(keycloak);
    assertThat(organizations.size(), is(1));

    rep = organizations.get(0);
    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getDisplayName(), CoreMatchers.nullValue());
    assertThat(rep.getUrl(), CoreMatchers.nullValue());
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // update
    rep.url("https://www.example.com/")
        .displayName("Example company")
        .attributes(ImmutableMap.of("foo", List.of("bar")));

    Response response = putRequest(rep, id);
    assertThat(response.statusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get single
    response = getRequest(id);
    rep = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getAttributes(), notNullValue());
    assertThat(rep.getAttributes().keySet(), hasSize(1));
    assertThat(rep.getAttributes().get("foo"), hasSize(1));
    assertThat(rep.getAttributes().get("foo").get(0), is("bar"));
    assertThat(rep.getDisplayName(), is("Example company"));
    assertThat(rep.getUrl(), is("https://www.example.com/"));
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // delete
    deleteOrganization(id);

    // get single
    response = getRequest(id);
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));

    // get list
    organizations = getListOfOrganizations(keycloak);
    assertThat(organizations.size(), is(0));
  }

  @Test
  void testDeleteAllOrganizations() throws Exception {
    final int numberOfOrgs = 1000;
    final String idpAlias = "idp-testDeleteAllOrganizations";
    final org.keycloak.representations.idm.IdentityProviderRepresentation idp = createIdentityProvider(idpAlias);
    for (int i = 0; i < numberOfOrgs; i++) {
      OrganizationRepresentation org = createOrganization(
          new OrganizationRepresentation().name("master" + i).domains(List.of("master" + i + ".com")));

      linkIdpWithOrg(idpAlias, org.getId());
    }
    // get list before delete
    List<OrganizationRepresentation> organizations1 = getListOfOrganizations(keycloak);
    assertThat(organizations1.size(), greaterThanOrEqualTo(100)); // get orgs endpoint returns max 100 organizations

    // delete
    deleteAllOrganizations();

    // get list after delete
    List<OrganizationRepresentation> organizations1AfterDelete = getListOfOrganizations(keycloak);
    assertThat(organizations1AfterDelete.size(), is(0));
  }

  private List<OrganizationRepresentation> getListOfOrganizations(Keycloak keycloak) throws IOException {
    Response getOrgsListResponse = getRequest(keycloak);
    assertThat(getOrgsListResponse.getStatusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRepresentation> organizations = objectMapper().readValue(getOrgsListResponse.getBody().asString(),
        new TypeReference<>() {
        });
    assertNotNull(organizations);
    return organizations;
  }

  @Test
  void testGetMe() throws Exception {
    OrganizationRepresentation org = createDefaultOrg();

    // for some reason the admin user can add
    // add admin user membership
    UserRepresentation admin =
        keycloak.realm(REALM).users().search(container.getAdminUsername()).get(0);

    Response response = putRequest("foo", org.getId(), "members", admin.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest("me");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    Map<String, Object> claim =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});

    assertThat(claim.keySet().size(), is(1));
    assertThat(claim.containsKey(org.getId()), is(true));
    Map<String, String> valueMap = (Map<String, String>) claim.values().stream().toList().get(0);
    assertThat(valueMap, hasEntry("name", "example"));
    assertThat(claim, hasKey(org.getId()));
    assertThat(valueMap, hasEntry("roles", newArrayList()));
    assertThat(valueMap, hasKey("attributes"));

    deleteOrganization(org.getId());
  }

  @Test
  void testSearchOrganizations() throws IOException {
    // create some orgs
    List<String> ids = new ArrayList<>();
    ids.add(
        createOrganization(
                new OrganizationRepresentation().name("example").domains(List.of("example.com")))
            .getId());
    ids.add(
        createOrganization(new OrganizationRepresentation().name("foo").domains(List.of("foo.com")))
            .getId());
    ids.add(
        createOrganization(
                new OrganizationRepresentation().name("foobar").domains(List.of("foobar.com")))
            .getId());
    ids.add(
        createOrganization(new OrganizationRepresentation().name("bar").domains(List.of("bar.com")))
            .getId());
    ids.add(
        createOrganization(
                new OrganizationRepresentation()
                    .name("baz")
                    .domains(List.of("baz.com"))
                    .attributes(Map.of("foo", List.of("bar"))))
            .getId());
    ids.add(
        createOrganization(
                new OrganizationRepresentation()
                    .name("qux")
                    .domains(List.of("baz.com"))
                    .attributes(Map.of("foo", List.of("bar"))))
            .getId());

    Response response = givenSpec().when().queryParam("search", "foo").get().andReturn();
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRepresentation> orgs =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(orgs, notNullValue());
    assertThat(orgs, hasSize(2));
    for (OrganizationRepresentation org : orgs) {
      assertThat(org.getName(), containsString("foo"));
      assertThat(org.getDomains(), hasSize(1));
    }

    response =
        givenSpec().when().queryParam("search", "foo").queryParam("max", 1).get().andReturn();
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(orgs, notNullValue());
    assertThat(orgs, hasSize(1));

    response = givenSpec().when().queryParam("search", "none").get().andReturn();
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(orgs, notNullValue());
    assertThat(orgs, hasSize(0));

    response = givenSpec().when().queryParam("search", "a").get().andReturn();
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(orgs, notNullValue());
    assertThat(orgs, hasSize(4));

    // orgs attribute search
    response = givenSpec().when().queryParam("q", "foo:bar").get().andReturn();
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(orgs, notNullValue());
    assertThat(orgs, hasSize(2));

    // Search attributes and name
    response =
        givenSpec().when().queryParam("search", "qu").queryParam("q", "foo:bar").get().andReturn();
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(orgs, notNullValue());
    assertThat(orgs, hasSize(1));

    // orgs count
    response = getRequest("count");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    Long cnt = objectMapper().readValue(response.getBody().asString(), Long.class);
    assertThat(orgs, notNullValue());
    assertThat(cnt, is(6L));

    for (String id : ids) {
      deleteOrganization(id);
    }
  }

  @Test
  void testGetDomains() throws IOException {

    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    Response response = getRequest(id, "domains");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationDomainRepresentation> domains =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(domains, notNullValue());
    assertThat(domains, hasSize(1));
    OrganizationDomainRepresentation domain = domains.get(0);
    assertThat(domain.getDomainName(), is("example.com"));
    assertThat(domain.getVerified(), is(false));
    assertThat(domain.getRecordKey(), notNullValue());
    assertThat(domain.getRecordValue(), notNullValue());
    log.infof(
        "domain %s %s %s", domain.getDomainName(), domain.getRecordKey(), domain.getRecordValue());

    // update
    org.domains(List.of("foo.com", "bar.net"));
    response = putRequest(org, id);
    assertThat(response.statusCode(), is(Status.NO_CONTENT.getStatusCode()));

    response = getRequest(id, "domains");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    domains = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(domains, notNullValue());
    assertThat(domains, hasSize(2));

    for (OrganizationDomainRepresentation d : domains) {
      assertThat(d.getDomainName(), oneOf("foo.com", "bar.net"));
      assertThat(d.getVerified(), is(false));
      assertThat(d.getRecordKey(), notNullValue());
      assertThat(d.getRecordValue(), notNullValue());
      log.infof("domain %s %s %s", d.getDomainName(), d.getRecordKey(), d.getRecordValue());
    }

    // verify
    response = postRequest("foo", id, "domains", "foo.com", "verify");
    assertThat(response.statusCode(), is(Status.ACCEPTED.getStatusCode()));

    // delete org
    deleteOrganization(id);
  }

  @Test
  @Disabled("currently not working with entra")
  void testImportConfig() throws Exception {
    OrganizationRepresentation org = createDefaultOrg();

    // import-config
    Map<String, Object> urlConf =
        ImmutableMap.of(
            "fromUrl",
            "https://login.microsoftonline.com/74df8381-4935-4fa8-8634-8e3413f93086/federationmetadata/2007-06/federationmetadata.xml?appid=ba149e64-4512-440b-a1b4-ae976d85f1ec",
            "providerId",
            "saml",
            "realm",
            REALM);

    Response response = postRequest(urlConf, org.getId(), "idps", "import-config");

    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    Map<String, Object> config =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(config, notNullValue());
    assertThat(config, hasEntry("loginHint", "false"));
    assertThat(config, hasEntry("postBindingLogout", "false"));
    assertThat(config, hasEntry("validateSignature", "false"));
    assertThat(config, hasEntry("wantAuthnRequestsSigned", "false"));
    // delete org
    deleteOrganization(org.getId());
  }

  @Test
  void testMembershipsCount() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();

    Response response = getRequest(org.getId(), "members", "count");
    Long memberCount = objectMapper().readValue(response.getBody().asString(), Long.class);

    assertThat(memberCount, is(1L)); // org admin default

    UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // add membership
    response = putRequest("foo", org.getId(), "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(org.getId(), "members", "count");
    memberCount = objectMapper().readValue(response.getBody().asString(), Long.class);

    assertThat(memberCount, is(2L));

    // delete org
    deleteOrganization(org.getId());
  }

  @Test
  void testAddGetDeleteMemberships() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    Response response = getRequest(id, "members");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));

    // get empty members list
    List<UserRepresentation> members =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(1)); // org admin default

    // create a user
    UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // check membership before add
    response = getRequest(id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));

    // add membership and check
    response = putRequest("foo", org.getId(), "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    response = getRequest(id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get members list
    response = getRequest(id, "members");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(2)); // +default org admin
    assertThat(members, hasItem(hasProperty("username", is("johndoe"))));

    // get user orgs
    response = givenSpec("users", user.getId(), "orgs").when().get().then().extract().response();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    List<OrganizationRepresentation> representations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(representations, notNullValue());
    assertThat(representations, hasSize(1));
    assertThat(representations.get(0).getName(), is("example"));

    // delete membership and check
    response = deleteRequest(id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    response = getRequest(id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));

    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // delete org
    deleteOrganization(id);
  }

  @Test
  void testSearchMembersWithMultipleNameParameter() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    Response response = getRequest(id, "members");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));

    // create a user
    UserRepresentation user1 = createUser(keycloak, REALM, "johndoe");
    UserRepresentation user2 = createUser(keycloak, REALM, "johndow");
    UserRepresentation user3 = createUser(keycloak, REALM, "jack");
    UserRepresentation user4 = createUser(keycloak, REALM, "jill");

    // add membership
    response = putRequest("foo", org.getId(), "members", user1.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    response = putRequest("foo", org.getId(), "members", user2.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    response = putRequest("foo", org.getId(), "members", user3.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    response = putRequest("foo", org.getId(), "members", user4.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(id, "members");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    List<UserRepresentation> members =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(5)); // including org admin default

    // search members with query parameter
    response = getRequest(id, "members?search=john");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(2));
    assertThat(members, hasItem(hasProperty("username", is("johndoe"))));
    assertThat(members, hasItem(hasProperty("username", is("johndow"))));

    response = getRequest(id, "members?search=jack,jill");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(2));
    assertThat(members, hasItem(hasProperty("username", is("jack"))));
    assertThat(members, hasItem(hasProperty("username", is("jill"))));

    response = getRequest(id, "members?search=john, jack, jill");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(4));
    assertThat(members, hasItem(hasProperty("username", is("johndoe"))));
    assertThat(members, hasItem(hasProperty("username", is("johndow"))));
    assertThat(members, hasItem(hasProperty("username", is("jack"))));
    assertThat(members, hasItem(hasProperty("username", is("jill"))));

    response = getRequest(id, "members?search=,,jack");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(1));
    assertThat(members, hasItem(hasProperty("username", is("jack"))));

    response = getRequest(id, "members?search=,, ,");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(0));

    response = getRequest(id, "members?search= ,, jack , ");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(1));
    assertThat(members, hasItem(hasProperty("username", is("jack"))));

    response = getRequest(id, "members?search=");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    members = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(members, notNullValue());
    assertThat(members, hasSize(5));

    // delete user
    deleteUser(keycloak, REALM, user1.getId());
    deleteUser(keycloak, REALM, user2.getId());
    deleteUser(keycloak, REALM, user3.getId());
    deleteUser(keycloak, REALM, user4.getId());

    // delete org
    deleteOrganization(id);
  }

  @Test
  void testDuplicateRoles() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    Response response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // get default roles list
    List<OrganizationRoleRepresentation> roles =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = "eat-apples";
    OrganizationRoleRepresentation roleRep = new OrganizationRoleRepresentation().name(name);
    response = postRequest(roleRep, id, "roles");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));

    // attempt to create same name role
    response = postRequest(roleRep, id, "roles");
    assertThat(response.getStatusCode(), is(Status.CONFLICT.getStatusCode()));

    // delete org
    deleteOrganization(id);
  }

  @Test
  void testAddGetDeleteRoles() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    // get default roles list
    Response response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRoleRepresentation> roles =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    ;
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String orgRoleName = "eat-apples";
    OrganizationRoleRepresentation orgRole = createOrgRole(id, orgRoleName);

    // get single role
    response = getRequest(id, "roles", orgRoleName);
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    OrganizationRoleRepresentation role =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(role, notNullValue());
    assertThat(role.getId(), notNullValue());
    assertThat(role.getName(), is(orgRoleName));

    // get role list
    response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    roles = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 1));

    // delete role
    response = deleteRequest(id, "roles", orgRoleName);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get single role 404
    response = getRequest(id, "roles", orgRoleName);
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));

    // create 3 roles
    String[] additionalRoles = {orgRoleName, "bake-pies", "view-fair"};
    for (String roleName : additionalRoles) {
      createOrgRole(id, roleName);
    }

    // get role list
    response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    roles = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 3));

    // create a user
    UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // add membership
    response = putRequest("foo", id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // grant role to user
    grantUserRole(id, orgRoleName, user.getId());

    // get users with role
    response = getRequest(id, "roles", orgRoleName, "users");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<UserRepresentation> rs =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(rs, notNullValue());
    assertThat(rs, hasSize(1));
    assertThat(rs.get(0).getUsername(), is("johndoe"));

    // check if user has role
    checkUserRole(id, orgRoleName, user.getId(), Status.NO_CONTENT.getStatusCode());

    // revoke role from user
    response = deleteRequest(id, "roles", orgRoleName, "users", user.getId());
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get users with role
    response = getRequest(id, "roles", orgRoleName, "users");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    rs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(rs, notNullValue());
    assertThat(rs, empty());

    // check if user has role
    checkUserRole(id, orgRoleName, user.getId(), Status.NOT_FOUND.getStatusCode());

    // grant more roles
    for (String roleName : additionalRoles) {
      grantUserRole(id, roleName, user.getId());
    }

    // check if user has role
    for (String roleName : additionalRoles) {
      checkUserRole(id, roleName, user.getId(), Status.NO_CONTENT.getStatusCode());
    }

    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // get users with role
    for (String roleName : additionalRoles) {
      response = getRequest(id, "roles", roleName, "users");
      assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
      rs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
      assertThat(rs, notNullValue());
      assertThat(rs, empty());
    }

    // delete roles
    for (String roleName : additionalRoles) {
      response = deleteRequest(id, "roles", roleName);
      assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    }

    // delete org
    deleteOrganization(id);
  }

  String webhookUrl() {
    return getAuthUrl() + "/realms/master/webhooks";
  }

  String eventsUrl() {
    return getAuthUrl() + "/realms/master/events";
  }

  @Test
  @Disabled("circular dependency with 25 working")
  public void testAddGetDeleteRolesBulk() throws Exception {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    // get default roles list
    Response response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRoleRepresentation> roles =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    CloseableHttpClient httpClient = HttpClients.createDefault();
    addEventListener(keycloak, "master", "ext-event-webhook");
    List<JsonNode> webhookEvents = new ArrayList<JsonNode>();
    Server webhookServer = new Server(WEBHOOK_SERVER_PORT);
    webhookServer
        .router()
        .POST(
            "/webhook",
            (request, resp) -> {
              String body = request.body();
              // log.infof("webhook: body %s", body);
              JsonNode node = objectMapper().readTree(body);
              webhookEvents.add(node);
              // log.infof("webhook: rep %s", node.toString());
              resp.body("OK");
              resp.status(200);
            });

    webhookServer.start();
    log.info("webhookServer: started");

    String webhookId =
        createWebhook(
            keycloak,
            httpClient,
            webhookUrl(),
            "http://host.testcontainers.internal:" + WEBHOOK_SERVER_PORT + "/webhook",
            "qlfwemke",
            List.of("admin.*"));

    // region CREATE ORG ROLES
    // create 3 bulk roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    String url = getAuthUrl() + "/realms/master/orgs/" + org.getId() + "/roles";
    List<OrganizationRole> roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("bake-pies"));
            add(new OrganizationRole().name("view-fair"));
          }
        };
    // log.infof("create 3 bulk roles req: %s", JsonSerialization.writeValueAsString(roleList));

    LegacySimpleHttp.Response resp =
        LegacySimpleHttp.doPut(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    // log.infof("create 3 bulk roles: %s", resp.asJson().toPrettyString());
    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(201));
            });

    Thread.sleep(1000l);
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE-CREATE"))
            .count(),
        is(3L));

    // get role list
    response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    roles = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 3));

    // create 2 already existing roles - everything fails
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("eat-apples"));
          }
        };
    resp =
        LegacySimpleHttp.doPut(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();
    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(400));
            });

    Thread.sleep(1000l);

    assertThat(webhookEvents.size(), is(0));

    // create 1 already existing role, 1 new role - some fail
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("drink-coffee"));
          }
        };
    resp =
        LegacySimpleHttp.doPut(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    assertThat(resp.asJson().get(0).get("status").asInt(), is(400));
    assertThat(resp.asJson().get(1).get("status").asInt(), is(201));

    Thread.sleep(1000l);

    assertThat(webhookEvents.size(), is(1));

    // get role list
    response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    roles = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 4));
    // endregion

    // region GRANT USER ORG ROLES
    // create a user
    UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // add membership
    response = putRequest("foo", id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // grant 2 exisiting bulk roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/users/" + user.getId() + "/orgs/" + org.getId() + "/roles";

    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("bake-pies"));
          }
        };

    resp =
        LegacySimpleHttp.doPut(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(201));
            });

    Thread.sleep(1000l);
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE_MAPPING-CREATE"))
            .count(),
        is(2L));

    // grant 1 already granted and 1 existing role
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/users/" + user.getId() + "/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("bake-pies"));
            add(new OrganizationRole().name("view-fair"));
          }
        };

    resp =
        LegacySimpleHttp.doPut(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    // log.infof("grant 1 already granted and 1 existing role resp: %s",
    // resp.asJson().toPrettyString());
    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(201));
            });

    Thread.sleep(1000l);
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE_MAPPING-CREATE"))
            .count(),
        is(1L));

    // grant 2 non-existing roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/users/" + user.getId() + "/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-fruits"));
            add(new OrganizationRole().name("drink-tea"));
          }
        };

    resp =
        LegacySimpleHttp.doPut(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(400));
            });
    // log.infof("grant 2 non-existing roles resp: %s", resp.asJson().toPrettyString());

    Thread.sleep(1000l);
    // webhookEvents.stream().forEach(i -> {
    //   log.infof("grant 2 non-existing roles event: %s", i.toPrettyString());
    // });
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE_MAPPING-CREATE"))
            .count(),
        is(0L));
    // endregion

    // region REVOKE USER ORG ROLES
    // revoke 2 non-existing roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/users/" + user.getId() + "/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-fruits"));
            add(new OrganizationRole().name("drink-tea"));
          }
        };

    resp =
        LegacySimpleHttp.doPatch(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(400));
            });
    // no events emitted
    Thread.sleep(1000l);
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE_MAPPING-DELETE"))
            .count(),
        is(0L));

    // revoke 2 granted roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/users/" + user.getId() + "/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("bake-pies"));
          }
        };

    resp =
        LegacySimpleHttp.doPatch(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(204));
            });

    Thread.sleep(1000l);
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE_MAPPING-DELETE"))
            .count(),
        is(2L));

    // revoke 1 already revoked and 1 granted role
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/users/" + user.getId() + "/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("bake-pies"));
            add(new OrganizationRole().name("view-fair"));
          }
        };

    resp =
        LegacySimpleHttp.doPatch(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(204));
            });

    Thread.sleep(1000l);
    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE_MAPPING-DELETE"))
            .count(),
        is(1L));

    // delete user
    deleteUser(keycloak, REALM, user.getId());
    Thread.sleep(1000l);
    // endregion

    // region DELETE ORG ROLES
    // delete 2 existing roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("drink-coffee"));
          }
        };
    log.infof("delete 2 existing roles req: %s", JsonSerialization.writeValueAsString(roleList));
    resp =
        LegacySimpleHttp.doPatch(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(204));
            });
    log.infof("delete 2 existing roles resp: %s", resp.asJson().toPrettyString());

    Thread.sleep(1000l);
    // webhookEvents.stream().forEach(i -> {
    //   log.infof("delete 2 existing roles event: %s", i.toPrettyString());
    // });

    assertThat(
        webhookEvents.stream()
            .filter(i -> i.get("type").asText().equals("admin.ORGANIZATION_ROLE-DELETE"))
            .count(),
        is(2L));

    // delete existing and non-existing roles
    webhookEvents.clear();

    Thread.sleep(1000l);

    url = getAuthUrl() + "/realms/master/orgs/" + org.getId() + "/roles";
    roleList =
        new ArrayList<>() {
          {
            add(new OrganizationRole().name("eat-apples"));
            add(new OrganizationRole().name("drink-coffee"));
            add(new OrganizationRole().name("bake-pies"));
            add(new OrganizationRole().name("view-fair"));
          }
        };
    resp =
        LegacySimpleHttp.doPatch(url, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleList)
            .asResponse();

    assertThat(resp.getStatus(), is(207));
    resp.asJson()
        .forEach(
            i -> {
              assertThat(i.get("status").asInt(), is(204));
            });

    Thread.sleep(1000l);
    assertThat(webhookEvents.stream().count(), is(4L));
    // endregion

    removeEventListener(keycloak, "master", "ext-event-webhook");

    webhookServer.stop();
    log.info("webhookServer: stopped");

    deleteWebhook(keycloak, httpClient, webhookUrl(), webhookId);

    // ensure we deleted all created roles
    response = getRequest(id, "roles");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    roles = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // delete org
    deleteOrganization(org.getId());
  }

  @Test
  void testAddGetDeleteInvitations() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    // create invitation
    InvitationRequest inv =
        new InvitationRequest()
            .email("johndoe@example.com")
            .attribute("foo", "bar")
            .attribute("foo", "bar2")
            .attribute("humpty", "dumpty");

    Response response = postRequest(inv, id, "invitations");
    assertThat(response.statusCode(), is(Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));
    String loc = response.getHeader("Location");
    String inviteId = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(inviteId, notNullValue());

    // get invitations
    response = getRequest(id, "invitations");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    List<Invitation> invites =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(invites, notNullValue());
    assertThat(invites, hasSize(1));
    assertThat(invites.get(0).getEmail(), is("johndoe@example.com"));
    assertThat(invites.get(0).getAttributes().size(), is(2));
    assertThat(invites.get(0).getAttributes().get("foo"), notNullValue());
    assertThat(invites.get(0).getAttributes().get("foo").size(), is(2));
    assertThat(invites.get(0).getAttributes().get("foo").get(0), is("bar"));
    assertThat(invites.get(0).getAttributes().get("foo").get(1), is("bar2"));
    assertThat(invites.get(0).getAttributes().get("humpty"), notNullValue());
    assertThat(invites.get(0).getAttributes().get("humpty").size(), is(1));
    assertThat(invites.get(0).getAttributes().get("humpty").get(0), is("dumpty"));
    String invId = invites.get(0).getId();

    // count invitations
    response = getRequest(id, "invitations", "count");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    Long cnt = objectMapper().readValue(response.getBody().asString(), Long.class);
    assertThat(cnt, is(1L));

    // get a specific innvitation
    response = getRequest(id, "invitations", invId);
    Invitation invite =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(invite.getEmail(), is("johndoe@example.com"));
    assertThat(invite.getAttributes().size(), is(2));
    assertThat(invite.getAttributes().get("foo"), notNullValue());
    assertThat(invite.getAttributes().get("foo").size(), is(2));
    assertThat(invite.getAttributes().get("foo").get(0), is("bar"));
    assertThat(invite.getAttributes().get("foo").get(1), is("bar2"));
    assertThat(invite.getAttributes().get("humpty"), notNullValue());
    assertThat(invite.getAttributes().get("humpty").size(), is(1));
    assertThat(invite.getAttributes().get("humpty").get(0), is("dumpty"));

    // try a conflicting invitation
    response = postRequest(inv, id, "invitations");
    assertThat(response.statusCode(), is(Status.CONFLICT.getStatusCode()));

    // remove pending invitation
    response = deleteRequest(id, "invitations", invId);
    assertThat(response.statusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // create user and give membership
    UserRepresentation user1 =
        createUserWithCredentials(keycloak, REALM, "user1", "pass", "johndoe@example.com");
    // grant membership to org
    response = putRequest("foo", id, "members", user1.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // try an invitation to that new user
    response = postRequest(inv, id, "invitations");
    assertThat(response.statusCode(), is(Status.CONFLICT.getStatusCode()));

    // get invitations
    response = getRequest(id, "invitations");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    invites = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(invites, notNullValue());
    assertThat(invites, empty());

    // count invitations, now 0
    response = getRequest(id, "invitations", "count");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    cnt = objectMapper().readValue(response.getBody().asString(), Long.class);
    assertThat(cnt, is(0L));

    // delete user
    deleteUser(keycloak, REALM, user1.getId());

    // delete org
    deleteOrganization(id);
  }

  @Test
  void testListOrgsByMember() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    // create some others too
    List<String> ids = new ArrayList<>();
    for (int n = 0; n < 150; n++) {
      OrganizationRepresentation organization =
          createOrganization(
              new OrganizationRepresentation().name("foo" + n).domains(List.of("foo.com")));
      ids.add(organization.getId());
    }

    // create user and give membership
    UserRepresentation user1 =
        createUserWithCredentials(keycloak, REALM, "user1", "pass", "johndoe@example.com");
    // grant membership to orgs
    Response response = putRequest("foo", id, "members", user1.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    String[] toJoin = {ids.get(11), ids.get(99), ids.get(100), ids.get(115), ids.get(149)};
    for (String i : toJoin) {
      response = putRequest("foo", i, "members", user1.getId());
      assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    }

    // log in as user
    Keycloak userKeycloak = getKeycloak(REALM, "admin-cli", "user1", "pass");

    // list orgs by admin
    response = getRequest();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRepresentation> orgs =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(orgs.size(), is(100));
    response = givenSpec().when().queryParam("first", 100).get().then().extract().response();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(orgs.size(), is(51));

    // list orgs by user
    response = getRequest(userKeycloak, "");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    orgs = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(orgs.size(), is(6));

    // delete user
    deleteUser(keycloak, REALM, user1.getId());

    // delete orgs
    deleteOrganization(id);

    for (String i : ids) {
      deleteOrganization(i);
    }
  }

  @Test
  void testLinkIdp() throws IOException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    String alias1 = "linking-provider-1";
    createIdentityProvider(alias1);

    // link it
    linkIdpWithOrg(alias1, org.getId());

    // get it
    Response response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));

    IdentityProviderRepresentation representation = idps.get(0);
    assertThat(representation.getEnabled(), is(true));
    assertThat(representation.getAlias(), is(alias1));
    assertThat(representation.getProviderId(), is("oidc"));
    assertThat(representation.getConfig().get("syncMode"), is("IMPORT"));
    String idpId = representation.getAlias();

    // unlink
    response = postRequest("foo", org.getId(), "idps", idpId, "unlink");
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // check it
    response = getRequest(id, "idps", idpId);
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));

    // delete org
    deleteOrganization(id);

    // delete idp
    keycloak.realm(REALM).identityProviders().get(alias1).remove();
  }

  @Test
  void testAddGetDeleteIdps() throws IOException {
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
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // get idps
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
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
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    ;

    // get idps
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    idps = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(2));
    for (IdentityProviderRepresentation i : idps) {
      assertThat(i.getEnabled(), is(i.getAlias().equals(alias2)));
    }

    // get mappers for idp
    response = getRequest(id, "idps", alias1, "mappers");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<IdentityProviderMapperRepresentation> mappers =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, empty());

    // add a mapper to the idp
    // {"identityProviderAlias":"oidc","config":
    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setIdentityProviderAlias(alias1);
    mapper.setName("name");
    mapper.setIdentityProviderMapper("oidc-user-attribute-idp-mapper");
    mapper.setConfig(
        new ImmutableMap.Builder<String, Object>()
            .put("syncMode", "INHERIT")
            .put("user.attribute", "name")
            .put("claim", "name")
            .build());
    response = postRequest(mapper, id, "idps", alias1, "mappers");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // get mappers for idp

    response = getRequest(id, "idps", alias1, "mappers");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    mappers = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, hasSize(1));
    String mapperId = mappers.get(0).getId();
    assertThat(mapperId, notNullValue());

    // get single mapper for idp
    response = getRequest(id, "idps", alias1, "mappers", mapperId);
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    mapper = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(mapper, notNullValue());
    assertThat(mapper.getName(), is("name"));
    assertThat(mapper.getIdentityProviderAlias(), is(alias1));
    assertThat(mapper.getIdentityProviderMapper(), is("oidc-user-attribute-idp-mapper"));

    // update mapper for idp
    mapper.setConfig(
        new ImmutableMap.Builder<String, Object>()
            .put("syncMode", "INHERIT")
            .put("user.attribute", "lastName")
            .put("claim", "familyName")
            .build());

    response = putRequest(mapper, id, "idps", alias1, "mappers", mapperId);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get single mapper for idp
    response = getRequest(id, "idps", alias1, "mappers", mapperId);
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    mapper = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(mapper, notNullValue());
    assertThat(mapper.getConfig().get("user.attribute"), is("lastName"));
    assertThat(mapper.getConfig().get("claim"), is("familyName"));

    // delete mappers for idp
    response = deleteRequest(id, "idps", alias1, "mappers", mapperId);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get mappers for idp
    response = getRequest(id, "idps", alias1, "mappers");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    mappers = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(mappers, empty());

    // delete idps
    response = deleteRequest(id, "idps", alias1);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    response = deleteRequest(id, "idps", alias2);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // get idps
    response = getRequest(id, "idps");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    idps = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, empty());

    // delete org
    deleteOrganization(id);
  }

  @Test
  void testIdpsOwnedOrgs() throws IOException {

    OrganizationRepresentation org =
        createOrganization(
            new OrganizationRepresentation().name("example").domains(List.of("example.com")));
    String orgId1 = org.getId();

    org =
        createOrganization(
            new OrganizationRepresentation().name("sample").domains(List.of("sample.com")));
    String orgId2 = org.getId();

    // create idp for org 1
    String alias1 = "vendor-protocol-A1";
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

    // create idp for org1
    Response response = postRequest(idp, orgId1, "idps");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // create idp for org 2
    String alias2 = "vendor-protocol-B1";
    idp.setAlias(alias2);
    response = postRequest(idp, orgId2, "idps");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // check that org 1 can only see idp 1
    response = getRequest(orgId1, "idps");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<IdentityProviderRepresentation> idps =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));
    assertThat(idps.get(0).getAlias(), is(alias1));

    // check that org 2 can only see idp 2
    response = getRequest(orgId2, "idps");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    idps = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));
    assertThat(idps.get(0).getAlias(), is(alias2));

    // check that org 1 cannot delete/update idp 2
    response = deleteRequest(orgId1, "idps", alias2);
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));

    // delete idp 1 explicitly
    response = deleteRequest(orgId1, "idps", alias1);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // delete orgs
    deleteOrganization(orgId1);
    deleteOrganization(orgId2);

    // try to delete idp 2
    response = deleteRequest(orgId2, "idps", alias2);
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  void testOrgAdminPermissions() throws IOException {
    OrganizationRepresentation rep = createDefaultOrg();
    String orgId1 = rep.getId();

    // create a normal user
    UserRepresentation user1 = createUserWithCredentials(keycloak, REALM, "user1", "pass");
    // grant membership to rep
    Response response = putRequest("foo", orgId1, "members", user1.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // grant rep admin permissions
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      grantUserRole(orgId1, role, user1.getId());
    }

    Keycloak kc1 = getKeycloak(REALM, "admin-cli", "user1", "pass");

    // check that user has permissions to update rep
    rep.url("https://www.example.com/").displayName("Example company");
    response = putRequest(kc1, rep, orgId1);
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    response = getRequest(kc1, orgId1);
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    rep = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getAttributes().keySet(), empty());
    assertThat(rep.getDisplayName(), is("Example company"));
    assertThat(rep.getUrl(), is("https://www.example.com/"));
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(orgId1));
    // get memberships
    response = getRequest(kc1, "/%s/members".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    // add memberships
    UserRepresentation user2 = createUserWithCredentials(keycloak, REALM, "user2", "pass");
    response = putRequest(kc1, "foo", "/%s/members/%s".formatted(orgId1, user2.getId()));
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    // remove memberships
    response = deleteRequest(kc1, "/%s/members/%s".formatted(orgId1, user2.getId()));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    // get invitations
    response = getRequest(kc1, "/%s/invitations".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    // add invitations
    InvitationRequestRepresentation inv =
        new InvitationRequestRepresentation().email("johndoe@example.com");
    response = postRequest(kc1, inv, "/%s/invitations".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    String loc = response.getHeader("Location");
    String inviteId = loc.substring(loc.lastIndexOf("/") + 1);
    // remove invitations
    response = deleteRequest(kc1, "/%s/invitations/%s".formatted(orgId1, inviteId));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    // get roles
    response = getRequest(kc1, "/%s/roles".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    // add roles
    createOrgRole(kc1, orgId1, "test-role");
    // remove roles
    response = deleteRequest(kc1, "/%s/roles/%s".formatted(orgId1, "test-role"));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    // add idp
    String alias1 = "org-admin-test";
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

    response = postRequest(kc1, idp, "/%s/idps".formatted(orgId1));
    assertThat(response.statusCode(), is(Status.CREATED.getStatusCode()));

    // get idp xxx
    response = getRequest("/%s/idps/%s".formatted(orgId1, alias1));
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
    IdentityProviderRepresentation idp1 =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(idp1, notNullValue());
    assertThat(idp1.getAlias(), is(alias1));
    assertThat(idp1.getProviderId(), is(idp.getProviderId()));
    assertThat(idp1.getEnabled(), is(true));
    assertThat(idp1.getFirstBrokerLoginFlowAlias(), is(idp.getFirstBrokerLoginFlowAlias()));
    assertThat(idp1.getConfig().get("clientId"), is(idp.getConfig().get("clientId")));

    // remove idp
    response = deleteRequest(kc1, "/%s/idps/%s".formatted(orgId1, alias1));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // create another org
    rep = new OrganizationRepresentation().name("sample").domains(List.of("sample.com"));
    OrganizationRepresentation org = createOrganization(rep);
    String orgId2 = org.getId();

    // check that user does not have permission to update rep
    rep.url("https://www.sample.com/").displayName("Sample company");
    response = putRequest(kc1, rep, orgId2);
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));

    // get memberships
    response = getRequest(kc1, "/%s/members".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // add memberships
    response = putRequest(kc1, "foo", "/%s/members/%s".formatted(orgId2, user2.getId()));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // remove memberships
    response = deleteRequest(kc1, "/%s/members/%s".formatted(orgId2, user2.getId()));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // get invitations
    response = getRequest(kc1, "/%s/invitations".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // add invitations
    response = postRequest(kc1, inv, "/%s/invitations".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // remove invitations
    response = deleteRequest(kc1, "/%s/invitations/%s".formatted(orgId2, inviteId));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // get roles
    response = getRequest(kc1, "/%s/roles".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // add roles
    response =
        postRequest(
            kc1,
            new OrganizationRoleRepresentation().name("test-role"),
            "/%s/roles".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    // remove roles
    response = deleteRequest(kc1, "/%s/roles/%s".formatted(orgId2, "test-role"));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));

    // delete user
    deleteUser(keycloak, REALM, user1.getId());

    // delete orgs
    deleteOrganization(orgId2);
    deleteOrganization(orgId1);
  }

  @Test
  void testOrgPortalLink() throws IOException {

    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    Response response =
        givenSpec()
            .header(new Header("content-type", MediaType.APPLICATION_FORM_URLENCODED))
            .formParam("baseUri", "foobar")
            .when()
            .post("/%s/portal-link".formatted(id))
            .then()
            .extract()
            .response();

    assertThat(response.getStatusCode(), is(Status.BAD_REQUEST.getStatusCode()));

    // create wizard client
    ClientRepresentation clientRepresentation = new ClientRepresentation();
    clientRepresentation.setName("idp-wizard");
    clientRepresentation.setId("idp-wizard");
    keycloak.realm(REALM).clients().create(clientRepresentation);
    response =
        givenSpec()
            .header(new Header("content-type", MediaType.APPLICATION_FORM_URLENCODED))
            .formParam("baseUri", "foobar")
            .when()
            .post("/%s/portal-link".formatted(id))
            .then()
            .extract()
            .response();

    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    UserRepresentation orgAdmin =
        keycloak.realm(REALM).users().search("org-admin-%s".formatted(id)).get(0);

    PortalLinkRepresentation link =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});

    assertThat(link, notNullValue());
    assertThat(link.getUser(), is(orgAdmin.getId()));
    assertThat(link.getLink(), notNullValue());
    assertThat(link.getRedirect(), containsString("foobar"));

    // delete org
    deleteOrganization(id);
  }

  @Test
  void testOrganizationSwitch() throws IOException, VerificationException {
    ObjectMapper mapper = objectMapper();

    // Org SETUP
    // Create first Organization
    OrganizationRepresentation org1 =
        createOrganization(
            new OrganizationRepresentation().name("org-1").domains(List.of("org1.com")));
    String org1Id = org1.getId();
    SwitchOrganization switchToOrganization1 = new SwitchOrganization().id(org1Id);

    // Create second Organization
    OrganizationRepresentation org2 =
        createOrganization(
            new OrganizationRepresentation().name("org-2").domains(List.of("org2.com")));
    String org2Id = org2.getId();
    SwitchOrganization switchToOrganization2 = new SwitchOrganization().id(org2Id);

    // Create third Organization
    OrganizationRepresentation org3 =
        createOrganization(
            new OrganizationRepresentation().name("org-3").domains(List.of("org3.com")));
    String org3Id = org3.getId();
    SwitchOrganization switchToOrganization3 = new SwitchOrganization().id(org3Id);

    // User SETUP
    // Add standard user
    UserRepresentation user = createUserWithCredentials(keycloak, REALM, "user", "password");
    // Assign manage-account role
    grantClientRoles("account", user.getId(), "manage-account", "view-profile");

    // Client SETUP
    // Create basic front end client to get a proper user access token
    createPublicClient("test-ui");
    createClientScope(ACTIVE_ORG_CLAIM);

    Map<String, String> additionalConfig =
        Map.of(INCLUDED_ORGANIZATION_PROPERTIES, "id, name, role, attribute");
    addMapperToClientScope(
        ACTIVE_ORG_CLAIM,
        ACTIVE_ORG_CLAIM,
        "JSON",
        "oidc-active-organization-mapper",
        additionalConfig);
    addClientScopeToClient(ACTIVE_ORG_CLAIM, "test-ui");

    // authenticate as standard user
    Keycloak kc = getKeycloak(REALM, "test-ui", "user", "password");

    // Get active organization with no organization membership
    Response response = getRequest(kc, "users", "active-organization");
    assertThat(response.getStatusCode(), is(Status.UNAUTHORIZED.getStatusCode()));

    // Add user to org1
    putRequest("pass", org1Id, "members", user.getId());

    // Try to remove membership with no active org attribute
    response = deleteRequest(org1Id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // Add again user to org1
    response = putRequest("pass", org1Id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      grantUserRole(org1Id, role, user.getId());
    }

    // Add user to org3
    response = putRequest("pass", org3Id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      grantUserRole(org3Id, role, user.getId());
    }

    // re-authenticate as standard user
    kc = getKeycloak(REALM, "test-ui", "user", "password");

    // validate that user currently doesn't have an active organization attribute
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), false, null);

    // validate current token claims, should have org-1 by default
    validateActiveOrganizationFromAccessToken(
        kc.tokenManager().getAccessTokenString(), true, org1Id);

    // validate get active-organization
    response = getRequest(kc, "users", "active-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // Try switching to organization 2
    response = putRequest(kc, switchToOrganization2, "users", "switch-organization");
    assertThat(response.getStatusCode(), is(Status.UNAUTHORIZED.getStatusCode()));

    // verify user attributes doesn't contains active organization
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), false, null);

    // Try switching to organization 3
    response = putRequest(kc, switchToOrganization3, "users", "switch-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // verify we get an access token with the correct organization
    JsonNode rootNode = mapper.readTree(response.body().asString());
    assertThat(rootNode.hasNonNull(ACCESS_TOKEN), is(true));
    validateActiveOrganizationFromAccessToken(rootNode.get(ACCESS_TOKEN).asText(), true, org3Id);

    // verify that user don't see the active organization attribute
    validateActiveOrganizationNotVisibleFromAccount(getUserAccount(kc));

    // verify user attributes contains active organization
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), true, org3Id);

    // validate get active-organization
    response = getRequest(kc, "users", "active-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // verify that standard user can't modify read-only org.ro.active attribute
    createOrReplaceReadOnlyUserAttribute(kc, "user", ACTIVE_ORGANIZATION, org2Id);

    // re-authenticate user to get new token and validate that the active organization didn't change
    kc = getKeycloak(REALM, "test-ui", "user", "password");
    validateActiveOrganizationFromAccessToken(
        kc.tokenManager().getAccessTokenString(), true, org3Id);

    // validate get active-organization after malicious organization switch
    response = getRequest(kc, "users", "active-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // test that active org attribute is removed when membership is revoked
    // switch back to org-1
    response = putRequest(kc, switchToOrganization1, "users", "switch-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // verify we get an access token with the correct organization
    rootNode = mapper.readTree(response.body().asString());
    assertThat(rootNode.hasNonNull(ACCESS_TOKEN), is(true));
    validateActiveOrganizationFromAccessToken(rootNode.get(ACCESS_TOKEN).asText(), true, org1Id);

    // verify user attributes contains active organization
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), true, org1Id);

    // validate get active-organization
    response = getRequest(kc, "users", "active-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // test revoke active organization membership
    response = deleteRequest(org1Id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // verify user attributes doesn't have active organization anymore
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), false, null);

    // test that active org attribute is removed after login when organization is deleted
    // switch to org3
    response = putRequest(kc, switchToOrganization3, "users", "switch-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // verify attribute
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), true, org3Id);

    // delete organization
    deleteOrganization(org3Id);

    // re-authenticate for lazy removal
    kc = getKeycloak(REALM, "test-ui", "user", "password");
    validateActiveOrganizationFromAccessToken(
        kc.tokenManager().getAccessTokenString(), false, null);

    // verify attribute is removed
    validateActiveOrganizationFromUserAttributes(getUser(user.getId()), false, null);

    deleteUser(keycloak, REALM, user.getId());
    deleteClient("test-ui");
    deleteClientScope(ACTIVE_ORG_CLAIM);
    deleteOrganization(org1Id);
    deleteOrganization(org2Id);
  }

  private void validateActiveOrganizationFromAccessToken(
      String accessTokenString, boolean shouldContain, String targetActiveOrgId)
      throws VerificationException {
    AccessToken decodedToken =
        TokenVerifier.create(accessTokenString, AccessToken.class).getToken();
    assertThat(decodedToken.getOtherClaims().isEmpty(), is(false));
    assertThat(decodedToken.getOtherClaims().containsKey(ACTIVE_ORG_CLAIM), is(true));

    @SuppressWarnings("unchecked")
    HashMap<String, Object> activeOrganizationClaims =
        (HashMap<String, Object>) decodedToken.getOtherClaims().get(ACTIVE_ORG_CLAIM);

    if (shouldContain) {
      assertThat(activeOrganizationClaims.containsKey("id"), is(true));
      assertThat(activeOrganizationClaims.get("id"), is(targetActiveOrgId));
    } else {
      assertThat(activeOrganizationClaims.isEmpty(), is(true));
    }
  }

  private void validateActiveOrganizationFromUserAttributes(
      Response response, boolean shouldContain, String targetOrgId) throws JsonProcessingException {
    JsonNode rootNode = objectMapper().readTree(response.body().asString());
    JsonNode attributeNode = rootNode.get("attributes");

    if (shouldContain) {
      assertThat(attributeNode == null, is(false));
      assertThat(attributeNode.hasNonNull(ACTIVE_ORGANIZATION), is(true));
      assertThat(attributeNode.get(ACTIVE_ORGANIZATION).get(0).asText(), is(targetOrgId));
    } else {
      if (attributeNode != null) {
        assertThat(attributeNode.hasNonNull(ACTIVE_ORGANIZATION), is(false));
      }
    }
  }

  private void validateActiveOrganizationNotVisibleFromAccount(Response response)
      throws JsonProcessingException {
    JsonNode rootNode = objectMapper().readTree(response.body().asString());
    JsonNode attributeNode = rootNode.get("attributes");
    if (attributeNode != null) {
      assertThat(attributeNode.hasNonNull(ACTIVE_ORGANIZATION), is(false));
    }
  }
}
