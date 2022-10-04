package io.phasetwo.service.resources;

import static io.phasetwo.service.Helpers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.phasetwo.service.KeycloakSuite;
import io.phasetwo.service.representation.Domain;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationRole;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class OrganizationResourceTest {

  @ClassRule public static KeycloakSuite server = KeycloakSuite.SERVER;

  CloseableHttpClient http = HttpClients.createDefault();

  String url(String realm, String... segments) {
    StringBuilder o = new StringBuilder();
    for (String segment : segments) {
      o.append("/").append(segment);
    }
    return String.format("%s/realms/%s/orgs%s", server.getAuthUrl(), realm, o.toString());
  }
  
  @Test
  public void testGetDomains() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    response =
        SimpleHttp.doGet(url("master", urlencode(id), "domains"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<Domain> domains = response.asJson(new TypeReference<List<Domain>>() {});
    assertNotNull(domains);
    assertThat(domains.size(), is(1));
    Domain domain = domains.get(0);
    assertThat(domain.getDomainName(), is("example.com"));
    assertFalse(domain.isVerified());
    assertNotNull(domain.getRecordKey());
    assertNotNull(domain.getRecordValue());
    log.infof(
        "domain %s %s %s", domain.getDomainName(), domain.getRecordKey(), domain.getRecordValue());

    // update
    rep.domains(ImmutableSet.of("foo.com", "bar.net"));
    response =
        SimpleHttp.doPut(url("master", urlencode(id)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(204));

    response =
        SimpleHttp.doGet(url("master", urlencode(id), "domains"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    domains = response.asJson(new TypeReference<List<Domain>>() {});
    assertNotNull(domains);
    assertThat(domains.size(), is(2));
    for (Domain d : domains) {
      assertTrue(d.getDomainName().equals("foo.com") || d.getDomainName().equals("bar.net"));
      assertFalse(d.isVerified());
      assertNotNull(d.getRecordKey());
      assertNotNull(d.getRecordValue());
      log.infof("domain %s %s %s", d.getDomainName(), d.getRecordKey(), d.getRecordValue());
    }

    // verify
    response =
        SimpleHttp.doPost(
                url("master", urlencode(id), "domains", urlencode("foo.com"), "verify"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .param("foo", "bar")
            .asResponse();
    assertThat(response.getStatus(), is(202));

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  public void testImportConfig() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    // import-config
    Map<String, String> urlConf =
        ImmutableMap.of(
            "fromUrl",
                "https://login.microsoftonline.com/74df8381-4935-4fa8-8634-8e3413f93086/federationmetadata/2007-06/federationmetadata.xml?appid=ba149e64-4512-440b-a1b4-ae976d85f1ec",
            "providerId", "saml",
            "realm", "master");
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "idps", "import-config"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(urlConf)
            .asResponse();
    assertThat(response.getStatus(), is(200));
    Map<String, String> config = response.asJson(new TypeReference<Map<String, String>>() {});
    log.infof("config %s", config);

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  public void testAddGetUpdateDeleteOrg() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    // get single
    response =
        SimpleHttp.doGet(url("master", urlencode(id)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    rep = response.asJson(new TypeReference<Organization>() {});
    assertNotNull(rep);
    assertNotNull(rep.getId());
    assertNull(rep.getDisplayName());
    assertNull(rep.getUrl());
    assertThat(rep.getRealm(), is("master"));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // get list
    response =
        SimpleHttp.doGet(url("master"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<Organization> orgs = response.asJson(new TypeReference<List<Organization>>() {});
    assertNotNull(orgs);
    assertThat(orgs.size(), is(1));
    rep = orgs.get(0);
    assertNotNull(rep.getId());
    assertNull(rep.getDisplayName());
    assertNull(rep.getUrl());
    assertThat(rep.getRealm(), is("master"));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // update
    rep.url("https://www.example.com/").displayName("Example company").attribute("foo", "bar");
    response =
        SimpleHttp.doPut(url("master", urlencode(id)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get single
    response =
        SimpleHttp.doGet(url("master", urlencode(id)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    rep = response.asJson(new TypeReference<Organization>() {});
    assertThat(response.getStatus(), is(200));
    assertNotNull(rep.getId());
    assertNotNull(rep.getAttributes());
    assertThat(rep.getAttributes().size(), is(1));
    assertThat(rep.getAttributes().get("foo").size(), is(1));
    assertThat(rep.getAttributes().get("foo").get(0), is("bar"));
    assertThat(rep.getDisplayName(), is("Example company"));
    assertThat(rep.getUrl(), is("https://www.example.com/"));
    assertThat(rep.getRealm(), is("master"));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // delete
    deleteOrg(keycloak, "master", id);

    // get single
    response =
        SimpleHttp.doGet(url("master", urlencode(id)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(404));

    // get list
    response =
        SimpleHttp.doGet(url("master"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    orgs = response.asJson(new TypeReference<List<Organization>>() {});
    assertNotNull(orgs);
    assertThat(orgs.size(), is(0));
  }

  @Test
  public void testAddGetDeleteMemberships() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    // get empty members list
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "members"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<UserRepresentation> members =
        response.asJson(new TypeReference<List<UserRepresentation>>() {});
    assertNotNull(members);
    assertThat(members.size(), is(1)); // org admin default

    // create a user
    UserRepresentation user = createUser(keycloak, "master", "johndoe");

    // check membership before add
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(404));

    // add membership
    response =
        SimpleHttp.doPut(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json("foo") // hack b/c simplehttp doesn't like body-less puts
            .asResponse();
    assertThat(response.getStatus(), is(201));

    // check membership after add
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get members list
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "members"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    members = response.asJson(new TypeReference<List<UserRepresentation>>() {});
    assertNotNull(members);
    assertThat(members.size(), is(2)); // +default org admin
    int idx = 0;
    if (members.get(idx).getUsername().startsWith("org")) idx++;
    assertThat(members.get(idx).getUsername(), is("johndoe"));

    // delete membership
    response =
        SimpleHttp.doDelete(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // check membership after delete
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(404));

    // add membership
    response =
        SimpleHttp.doPut(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json("foo") // hack b/c simplehttp doesn't like body-less puts
            .asResponse();
    assertThat(response.getStatus(), is(201));

    // call the users/:id/orgs endpoint
    String userOrgsUrl =
        String.format("%s/realms/%s/users/%s/orgs", server.getAuthUrl(), "master", user.getId());
    response =
        SimpleHttp.doGet(userOrgsUrl, http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<Organization> orgs = response.asJson(new TypeReference<List<Organization>>() {});
    assertNotNull(orgs);
    assertThat(orgs.size(), is(1));
    assertThat(orgs.get(0).getName(), is("example"));

    // delete user
    deleteUser(keycloak, "master", user.getId());

    // check membership after delete user
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(404));

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  public void testDuplicateRoles() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    // get default roles list
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<OrganizationRole> roles = response.asJson(new TypeReference<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = "eat-apples";
    OrganizationRole roleRep = new OrganizationRole().name(name);
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleRep)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getFirstHeader("Location"));

    // attempt to create same name role
    roleRep = new OrganizationRole().name(name);
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(roleRep)
            .asResponse();
    assertThat(response.getStatus(), is(409));

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  public void testAddGetDeleteRoles() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    // get default roles list
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<OrganizationRole> roles = response.asJson(new TypeReference<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = createRole(keycloak, id, "eat-apples");

    // get single role
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles", urlencode(name)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    OrganizationRole role = response.asJson(new TypeReference<OrganizationRole>() {});
    assertNotNull(role);
    assertNotNull(role.getId());
    assertThat(role.getName(), is("eat-apples"));

    // get role list
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    roles = response.asJson(new TypeReference<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 1));

    // delete role
    response =
        SimpleHttp.doDelete(url("master", urlencode(id), "roles", urlencode(name)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get single role 404
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles", urlencode(name)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(404));

    // create 3 roles
    String[] ra = {"eat-apples", "bake-pies", "view-fair"};
    for (String r : ra) {
      createRole(keycloak, id, r);
    }

    // get role list
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    roles = response.asJson(new TypeReference<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 3));

    // create a user
    UserRepresentation user = createUser(keycloak, "master", "johndoe");

    // add membership
    response =
        SimpleHttp.doPut(url("master", urlencode(id), "members", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json("foo") // hack b/c simplehttp doesn't like body-less puts
            .asResponse();
    assertThat(response.getStatus(), is(201));

    // grant role to user
    grantUserRole(keycloak, id, name, user.getId());

    // get users with role
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles", urlencode(name), "users"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<UserRepresentation> rs = response.asJson(new TypeReference<List<UserRepresentation>>() {});
    assertNotNull(rs);
    assertThat(rs.size(), is(1));
    assertThat(rs.get(0).getUsername(), is("johndoe"));

    // check if user has role
    checkUserRole(keycloak, id, name, user.getId(), 204);

    // revoke role from user
    response =
        SimpleHttp.doDelete(
                url("master", urlencode(id), "roles", urlencode(name), "users", user.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get users with role
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "roles", urlencode(name), "users"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    rs = response.asJson(new TypeReference<List<UserRepresentation>>() {});
    assertNotNull(rs);
    assertThat(rs.size(), is(0));

    // check if user has role
    checkUserRole(keycloak, id, name, user.getId(), 404);

    // grant more roles
    for (String r : ra) {
      grantUserRole(keycloak, id, r, user.getId());
    }

    // check if user has role
    for (String r : ra) {
      checkUserRole(keycloak, id, r, user.getId(), 204);
    }

    // delete user
    deleteUser(keycloak, "master", user.getId());

    // get users with role
    for (String r : ra) {
      response =
          SimpleHttp.doGet(url("master", urlencode(id), "roles", urlencode(r), "users"), http)
              .auth(keycloak.tokenManager().getAccessTokenString())
              .asResponse();
      assertThat(response.getStatus(), is(200));
      rs = response.asJson(new TypeReference<List<UserRepresentation>>() {});
      assertNotNull(rs);
      assertThat(rs.size(), is(0));
    }

    // delete roles
    for (String r : ra) {
      response =
          SimpleHttp.doDelete(url("master", urlencode(id), "roles", urlencode(r)), http)
              .auth(keycloak.tokenManager().getAccessTokenString())
              .asResponse();
      assertThat(response.getStatus(), is(204));
    }

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  public void testAddGetDeleteInvitations() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    // create invitation
    InvitationRequest inv = new InvitationRequest().email("johndoe@example.com");
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "invitations"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(inv)
            .asResponse();
    assertThat(response.getStatus(), is(201));

    // get invitations
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "invitations"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<Invitation> invites = response.asJson(new TypeReference<List<Invitation>>() {});
    assertNotNull(invites);
    assertThat(invites.size(), is(1));
    assertThat(invites.get(0).getEmail(), is("johndoe@example.com"));
    String invId = invites.get(0).getId();

    // try a conflicting invitation
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "invitations"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(inv)
            .asResponse();
    assertThat(response.getStatus(), is(409));
    
    // remove pending invitation
    response =
        SimpleHttp.doDelete(url("master", urlencode(id), "invitations", urlencode(invId)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // create user and give membership
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType("password");
    pass.setValue("pass");
    pass.setTemporary(false);
    UserRepresentation user1 = new UserRepresentation();
    user1.setEnabled(true);
    user1.setUsername("user1");
    user1.setEmail("johndoe@example.com");
    user1.setCredentials(ImmutableList.of(pass));
    user1 = createUser(keycloak, "master", user1);
    // grant membership to org
    response =
        SimpleHttp.doPut(url("master", urlencode(id), "members", user1.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json("foo") // hack b/c simplehttp doesn't like body-less puts
            .asResponse();
    assertThat(response.getStatus(), is(201));

    // try an invitation to that new user
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "invitations"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(inv)
            .asResponse();
    assertThat(response.getStatus(), is(409));
    
    // get invitations
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "invitations"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    invites = response.asJson(new TypeReference<List<Invitation>>() {});
    assertNotNull(invites);
    assertThat(invites.size(), is(0));

    // delete user
    deleteUser(keycloak, "master", user1.getId());

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  public void testAddGetDeleteIdps() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias("vendor-protocol-1");
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
        new ImmutableMap.Builder<String, String>()
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
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(idp)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    String loc = response.getFirstHeader("Location");
    String alias1 = loc.substring(loc.lastIndexOf("/") + 1);

    // get idps
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<IdentityProviderRepresentation> idps =
        response.asJson(new TypeReference<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(1));
    assertTrue(idps.get(0).isEnabled());
    // assertThat(idps.get(0).getAlias(), is("test-oidc-01"));
    assertThat(idps.get(0).getAlias(), is(alias1));
    assertThat(idps.get(0).getProviderId(), is("oidc"));

    // create idp
    idp.setAlias("vendor-protocol-2");
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(idp)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    loc = response.getFirstHeader("Location");
    String alias2 = loc.substring(loc.lastIndexOf("/") + 1);

    // get idps
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    idps = response.asJson(new TypeReference<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(2));
    for (IdentityProviderRepresentation i : idps) {
      if (i.getAlias().equals(alias2)) {
        assertTrue(i.isEnabled());
      } else {
        assertFalse(i.isEnabled());
      }
    }

    // get mappers for idp
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "idps", urlencode(alias1), "mappers"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<IdentityProviderMapperRepresentation> mappers =
        response.asJson(new TypeReference<List<IdentityProviderMapperRepresentation>>() {});
    assertThat(mappers.size(), is(0));

    // add a mapper to the idp
    //    {"identityProviderAlias":"oidc","config":
    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setIdentityProviderAlias(alias1);
    mapper.setName("name");
    mapper.setIdentityProviderMapper("oidc-user-attribute-idp-mapper");
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("user.attribute", "name")
            .put("claim", "name")
            .build());
    response =
        SimpleHttp.doPost(url("master", urlencode(id), "idps", urlencode(alias1), "mappers"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(mapper)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    loc = response.getFirstHeader("Location");
    String mapperId = loc.substring(loc.lastIndexOf("/") + 1);

    // get single mapper for idp
    response =
        SimpleHttp.doGet(
                url(
                    "master",
                    urlencode(id),
                    "idps",
                    urlencode(alias1),
                    "mappers",
                    urlencode(mapperId)),
                http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    mapper = response.asJson(new TypeReference<IdentityProviderMapperRepresentation>() {});
    assertNotNull(mapper);
    assertThat(mapper.getName(), is("name"));
    assertThat(mapper.getIdentityProviderAlias(), is(alias1));
    assertThat(mapper.getIdentityProviderMapper(), is("oidc-user-attribute-idp-mapper"));

    // get mappers for idp
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "idps", urlencode(alias1), "mappers"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    mappers = response.asJson(new TypeReference<List<IdentityProviderMapperRepresentation>>() {});
    assertThat(mappers.size(), is(1));

    // update mapper for idp
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("user.attribute", "lastName")
            .put("claim", "familyName")
            .build());
    response =
        SimpleHttp.doPut(
                url(
                    "master",
                    urlencode(id),
                    "idps",
                    urlencode(alias1),
                    "mappers",
                    urlencode(mapperId)),
                http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(mapper)
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get single mapper for idp
    response =
        SimpleHttp.doGet(
                url(
                    "master",
                    urlencode(id),
                    "idps",
                    urlencode(alias1),
                    "mappers",
                    urlencode(mapperId)),
                http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    mapper = response.asJson(new TypeReference<IdentityProviderMapperRepresentation>() {});
    assertNotNull(mapper);
    assertThat(mapper.getConfig().get("user.attribute"), is("lastName"));
    assertThat(mapper.getConfig().get("claim"), is("familyName"));

    // delete mappers for idp
    response =
        SimpleHttp.doDelete(
                url(
                    "master",
                    urlencode(id),
                    "idps",
                    urlencode(alias1),
                    "mappers",
                    urlencode(mapperId)),
                http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get mappers for idp
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "idps", urlencode(alias1), "mappers"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    mappers = response.asJson(new TypeReference<List<IdentityProviderMapperRepresentation>>() {});
    assertThat(mappers.size(), is(0));

    // delete idps
    response =
        SimpleHttp.doDelete(url("master", urlencode(id), "idps", urlencode(alias1)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));
    response =
        SimpleHttp.doDelete(url("master", urlencode(id), "idps", urlencode(alias2)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // get idps
    response =
        SimpleHttp.doGet(url("master", urlencode(id), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    idps = response.asJson(new TypeReference<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(0));

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  @Test
  @Ignore
  public void testIdpsOwnedOrgs() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    // create one org
    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String orgId1 = createOrg(keycloak, "master", rep);

    // create another org
    rep = new Organization().name("sample").domains(ImmutableSet.of("sample.com"));
    String orgId2 = createOrg(keycloak, "master", rep);

    // create idp for org 1
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias("vendor-protocol-A1");
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
        new ImmutableMap.Builder<String, String>()
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
    response =
        SimpleHttp.doPost(url("master", urlencode(orgId1), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(idp)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    String loc = response.getFirstHeader("Location");
    String alias1 = loc.substring(loc.lastIndexOf("/") + 1);

    // create idp for org 2
    idp.setAlias("vendor-protocol-B1");
    response =
        SimpleHttp.doPost(url("master", urlencode(orgId2), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(idp)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    loc = response.getFirstHeader("Location");
    String alias2 = loc.substring(loc.lastIndexOf("/") + 1);

    // check that org 1 can only see idp 1
    response =
        SimpleHttp.doGet(url("master", urlencode(orgId1), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    List<IdentityProviderRepresentation> idps =
        response.asJson(new TypeReference<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(1));
    assertThat(idps.get(0).getAlias(), is(alias1));

    // check that org 2 can only see idp 2
    response =
        SimpleHttp.doGet(url("master", urlencode(orgId2), "idps"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(200));
    idps = response.asJson(new TypeReference<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(1));
    assertThat(idps.get(0).getAlias(), is(alias2));

    // check that org 1 cannot delete/update idp 2
    response =
        SimpleHttp.doDelete(url("master", urlencode(orgId1), "idps", urlencode(alias2)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), not(200));

    // delete idps 1 & 2
    response =
        SimpleHttp.doDelete(url("master", urlencode(orgId1), "idps", urlencode(alias1)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));
    response =
        SimpleHttp.doDelete(url("master", urlencode(orgId2), "idps", urlencode(alias2)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // delete org 2
    deleteOrg(keycloak, "master", orgId2);

    // delete org 1
    deleteOrg(keycloak, "master", orgId1);
  }

  @Test
  public void testOrgAdminPermissions() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    // create one org
    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String orgId1 = createOrg(keycloak, "master", rep);

    // create a normal user
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType("password");
    pass.setValue("pass");
    pass.setTemporary(false);
    UserRepresentation user1 = new UserRepresentation();
    user1.setEnabled(true);
    user1.setUsername("user1");
    user1.setCredentials(ImmutableList.of(pass));
    user1 = createUser(keycloak, "master", user1);
    // grant membership to org
    response =
        SimpleHttp.doPut(url("master", urlencode(orgId1), "members", user1.getId()), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json("foo") // hack b/c simplehttp doesn't like body-less puts
            .asResponse();
    assertThat(response.getStatus(), is(201));

    // grant org admin permissions
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      grantUserRole(keycloak, orgId1, role, user1.getId());
    }

    Keycloak kc1 = server.client("master", "admin-cli", "user1", "pass");
    // check that user has permissions to
    //  update org
    rep.url("https://www.example.com/").displayName("Example company");
    response =
        SimpleHttp.doPut(url("master", urlencode(orgId1)), http)
            .auth(kc1.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(204));
    response =
        SimpleHttp.doGet(url("master", urlencode(orgId1)), http)
            .auth(kc1.tokenManager().getAccessTokenString())
            .asResponse();
    rep = response.asJson(new TypeReference<Organization>() {});
    assertThat(response.getStatus(), is(200));
    assertNotNull(rep.getId());
    assertThat(rep.getAttributes().size(), is(0));
    assertThat(rep.getDisplayName(), is("Example company"));
    assertThat(rep.getUrl(), is("https://www.example.com/"));
    assertThat(rep.getRealm(), is("master"));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(orgId1));
    //  get memberships
    //  add memberships
    //  remove memberships
    //  get invitations
    //  add invitations
    //  remove invitations
    //  get roles
    //  add roles
    //  remove roles
    //  add idp
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias("org-admin-test");
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
        new ImmutableMap.Builder<String, String>()
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
    response =
        SimpleHttp.doPost(url("master", urlencode(orgId1), "idps"), http)
            .auth(kc1.tokenManager().getAccessTokenString())
            .json(idp)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    String loc = response.getFirstHeader("Location");
    String alias1 = loc.substring(loc.lastIndexOf("/") + 1);
    //  get idp xxx
    response =
        SimpleHttp.doGet(url("master", urlencode(orgId1), "idps", urlencode(alias1)), http)
            .auth(kc1.tokenManager().getAccessTokenString())
            .asResponse();
    IdentityProviderRepresentation idp1 =
        response.asJson(new TypeReference<IdentityProviderRepresentation>() {});
    assertThat(response.getStatus(), is(200));
    assertThat(idp1.getAlias(), is(alias1));
    assertThat(idp1.getProviderId(), is(idp.getProviderId()));
    assertTrue(idp1.isEnabled());
    assertThat(idp1.getFirstBrokerLoginFlowAlias(), is(idp.getFirstBrokerLoginFlowAlias()));
    assertThat(idp1.getConfig().get("clientId"), is(idp.getConfig().get("clientId")));
    //  remove idp
    response =
        SimpleHttp.doDelete(url("master", urlencode(orgId1), "idps", urlencode(alias1)), http)
            .auth(kc1.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));

    // create another org
    rep = new Organization().name("sample").domains(ImmutableSet.of("sample.com"));
    String orgId2 = createOrg(keycloak, "master", rep);

    // check that user does not have permission to
    //  update org
    rep.url("https://www.sample.com/").displayName("Sample company");
    response =
        SimpleHttp.doPut(url("master", urlencode(orgId2)), http)
            .auth(kc1.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(401));
    //  get memberships
    //  add memberships
    //  remove memberships
    //  get invitations
    //  add invitations
    //  remove invitations
    //  get roles
    //  add roles
    //  remove roles

    // delete user
    deleteUser(keycloak, "master", user1.getId());

    // delete other org
    deleteOrg(keycloak, "master", orgId2);

    // delete org
    deleteOrg(keycloak, "master", orgId1);
  }

  @Test
  @Ignore
  public void testOrgPortalLink() throws Exception {
    Keycloak keycloak = server.client();
    SimpleHttp.Response response = null;

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(keycloak, "master", rep);

    response =
        SimpleHttp.doPost(url("master", urlencode(id), "portal-link"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .param("foo", "bar") // simplehttp doesnt like empty requests for post
            .asResponse();

    assertThat(response.getStatus(), is(200));
    Map<String, String> resp = response.asJson(new TypeReference<Map<String, String>>() {});
    assertNotNull(resp.get("user"));
    assertNotNull(resp.get("link"));
    assertNotNull(resp.get("redirect"));

    System.err.println(String.format("portal-link %s", resp));

    // delete org
    deleteOrg(keycloak, "master", id);
  }

  private String createRole(Keycloak keycloak, String orgId, String name) throws Exception {
    OrganizationRole rep = new OrganizationRole().name(name);
    SimpleHttp.Response response =
        SimpleHttp.doPost(url("master", urlencode(orgId), "roles"), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getFirstHeader("Location"));
    String loc = response.getFirstHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(name, is(id));
    return id;
  }

  private void grantUserRole(Keycloak keycloak, String orgId, String role, String userId)
      throws Exception {
    // grant role to user
    // PUT /:realm/orgs/:orgId/roles/:name/users/:userId
    SimpleHttp.Response response =
        SimpleHttp.doPut(
                url("master", urlencode(orgId), "roles", urlencode(role), "users", userId), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json("foo") // hack b/c simplehttp doesn't like body-less puts
            .asResponse();
    assertThat(response.getStatus(), is(201));
  }

  private void checkUserRole(
      Keycloak keycloak, String orgId, String role, String userId, int status) throws Exception {
    // check if user has role
    SimpleHttp.Response response =
        SimpleHttp.doGet(
                url("master", urlencode(orgId), "roles", urlencode(role), "users", userId), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(status));
  }

  private String createOrg(Keycloak keycloak, String realm, Organization rep) throws Exception {
    SimpleHttp.Response response =
        SimpleHttp.doPost(url(realm), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getFirstHeader("Location"));
    String loc = response.getFirstHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);
    assertNotNull(id);
    return id;
  }

  private void deleteOrg(Keycloak keycloak, String realm, String orgId) throws Exception {
    SimpleHttp.Response response =
        SimpleHttp.doDelete(url(realm, urlencode(orgId)), http)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));
  }
}
