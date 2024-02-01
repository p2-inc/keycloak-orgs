package io.phasetwo.service.resource;

import static com.google.common.collect.Lists.newArrayList;
import static io.phasetwo.service.Helpers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.IdentityProviderMapperRepresentation;
import io.phasetwo.client.openapi.model.IdentityProviderRepresentation;
import io.phasetwo.client.openapi.model.InvitationRequestRepresentation;
import io.phasetwo.client.openapi.model.OrganizationDomainRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.client.openapi.model.PortalLinkRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.representation.LinkIdp;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Testcontainers;

@JBossLog
@Testcontainers
class OrganizationResourceTest extends AbstractOrganizationTest {

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
    Response response = getRequest();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRepresentation> organizations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertNotNull(organizations);
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

    response = putRequest(rep, id);
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
    response = getRequest();

    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    organizations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertNotNull(organizations);
    assertThat(organizations.size(), is(0));
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
    ;
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
  void testImportConfig() throws Exception {
    OrganizationRepresentation org = createDefaultOrg();

    // import-config
    Map<String, Object> urlConf =
        ImmutableMap.of(
            "fromUrl",
                "https://login.microsoftonline.com/74df8381-4935-4fa8-8634-8e3413f93086/federationmetadata/2007-06/federationmetadata.xml?appid=ba149e64-4512-440b-a1b4-ae976d85f1ec",
            "providerId", "saml",
            "realm", REALM);
    Response response = postRequest(urlConf, org.getId(), "idps", "import-config");

    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    Map<String, Object> config =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(config, notNullValue());
    assertThat(config.keySet(), hasSize(11));
    assertThat(config, hasEntry("loginHint", "false"));
    assertThat(config, hasEntry("postBindingLogout", "false"));
    assertThat(config, hasEntry("validateSignature", "false"));
    assertThat(config, hasEntry("wantAuthnRequestsSigned", "false"));

    // import-config manually
    urlConf =
        ImmutableMap.of(
            "fromUrl",
                "https://login.microsoftonline.com/75a21e21-e75f-46cd-81a1-73b0486c7e81/federationmetadata/2007-06/federationmetadata.xml?appid=65032359-8102-4ff8-aed0-005752ce97ff",
            "providerId", "saml",
            "realm", REALM);
    response = postRequest(urlConf, org.getId(), "idps", "import-config");

    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    config = objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(config, notNullValue());
    assertThat(config.keySet(), hasSize(11));
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
    // xxx
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
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // get it
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
    assertThat(representation.getConfig().get("syncMode"), is("IMPORT"));

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
    //    {"identityProviderAlias":"oidc","config":
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
    //  get memberships
    response = getRequest(kc1, "/%s/members".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    //  add memberships
    UserRepresentation user2 = createUserWithCredentials(keycloak, REALM, "user2", "pass");
    response = putRequest(kc1, "foo", "/%s/members/%s".formatted(orgId1, user2.getId()));
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    //  remove memberships
    response = deleteRequest(kc1, "/%s/members/%s".formatted(orgId1, user2.getId()));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    //  get invitations
    response = getRequest(kc1, "/%s/invitations".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    //  add invitations
    InvitationRequestRepresentation inv =
        new InvitationRequestRepresentation().email("johndoe@example.com");
    response = postRequest(kc1, inv, "/%s/invitations".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    String loc = response.getHeader("Location");
    String inviteId = loc.substring(loc.lastIndexOf("/") + 1);
    //  remove invitations
    response = deleteRequest(kc1, "/%s/invitations/%s".formatted(orgId1, inviteId));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    //  get roles
    response = getRequest(kc1, "/%s/roles".formatted(orgId1));
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    //  add roles
    createOrgRole(kc1, orgId1, "test-role");
    //  remove roles
    response = deleteRequest(kc1, "/%s/roles/%s".formatted(orgId1, "test-role"));
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
    //  add idp
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

    //  get idp xxx
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

    //  remove idp
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

    //  get memberships
    response = getRequest(kc1, "/%s/members".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  add memberships
    response = putRequest(kc1, "foo", "/%s/members/%s".formatted(orgId2, user2.getId()));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  remove memberships
    response = deleteRequest(kc1, "/%s/members/%s".formatted(orgId2, user2.getId()));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  get invitations
    response = getRequest(kc1, "/%s/invitations".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  add invitations
    response = postRequest(kc1, inv, "/%s/invitations".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  remove invitations
    response = deleteRequest(kc1, "/%s/invitations/%s".formatted(orgId2, inviteId));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  get roles
    response = getRequest(kc1, "/%s/roles".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  add roles
    response =
        postRequest(
            kc1,
            new OrganizationRoleRepresentation().name("test-role"),
            "/%s/roles".formatted(orgId2));
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
    //  remove roles
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
}
