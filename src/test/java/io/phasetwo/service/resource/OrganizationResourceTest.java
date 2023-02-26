package io.phasetwo.service.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.phasetwo.service.openapi.PhaseTwo;
import io.phasetwo.service.openapi.api.IdentityProvidersApi;
import io.phasetwo.service.openapi.api.OrganizationDomainsApi;
import io.phasetwo.service.openapi.api.OrganizationInvitationsApi;
import io.phasetwo.service.openapi.api.OrganizationMembershipsApi;
import io.phasetwo.service.openapi.api.OrganizationRolesApi;
import io.phasetwo.service.openapi.api.OrganizationsApi;
import io.phasetwo.service.openapi.api.UsersApi;
import io.phasetwo.service.representation.Domain;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationRole;
import lombok.extern.jbosslog.JBossLog;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.deleteUser;
import static io.phasetwo.service.Helpers.urlencode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@JBossLog
public class OrganizationResourceTest extends AbstractResourceTest {
  Response response;
  
  @After
  public void close() {
    // trick to avoid 'try-with-resources statement' intellij warning
    if (response != null) {
      response.close();
    }
  }

  @Test
  public void testRealmId() {
    try(Keycloak keycloak = server.client()) {
      RealmRepresentation r = keycloak.realm(REALM).toRepresentation();
      assertThat(r.getRealm(), is(REALM));
      assertThat(r.getId(), not(REALM));
    }
  }
  
  @Test
  public void testGetDomains() {
    PhaseTwo client = phaseTwo();
    OrganizationDomainsApi domainsApi = client.domains();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    response = domainsApi.getOrganizationDomains(REALM, id);
    assertThat(response.getStatus(), is(200));
    List<Domain> domains = response.readEntity(new GenericType<List<Domain>>() {});
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
    response = organizationsApi.updateOrganization(REALM, id, rep);
    assertThat(response.getStatus(), is(204));

    response = domainsApi.getOrganizationDomains(REALM, id);
    assertThat(response.getStatus(), is(200));
    domains = response.readEntity(new GenericType<List<Domain>>() {});
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
    response = domainsApi.verifyDomain(REALM, id, "foo.com");
    assertThat(response.getStatus(), is(202));

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  public void testImportConfig() {
    PhaseTwo client = phaseTwo();
    IdentityProvidersApi idpsApi = client.idps();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    // import-config
    Map<String, String> urlConf =
        ImmutableMap.of(
            "fromUrl",
                "https://login.microsoftonline.com/74df8381-4935-4fa8-8634-8e3413f93086/federationmetadata/2007-06/federationmetadata.xml?appid=ba149e64-4512-440b-a1b4-ae976d85f1ec",
            "providerId", "saml",
            "realm", REALM);
    response = idpsApi.importIdpJson(REALM, id, urlConf);
    assertThat(response.getStatus(), is(200));
    Map<String, String> config = response.readEntity(new GenericType<Map<String, String>>() {});
    log.infof("config %s", config);

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  public void testAddGetUpdateDeleteOrg() {
    PhaseTwo client = phaseTwo();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    // get single
    response = organizationsApi.getOrganizationById(REALM, id);
    assertThat(response.getStatus(), is(200));
    rep = response.readEntity(new GenericType<Organization>() {});
    assertNotNull(rep);
    assertNotNull(rep.getId());
    assertNull(rep.getDisplayName());
    assertNull(rep.getUrl());
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // get list
    response = organizationsApi.getOrganizations(REALM, null, 0, 10);
    assertThat(response.getStatus(), is(200));
    List<Organization> orgs = response.readEntity(new GenericType<List<Organization>>() {});
    assertNotNull(orgs);
    assertThat(orgs.size(), is(1));
    rep = orgs.get(0);
    assertNotNull(rep.getId());
    assertNull(rep.getDisplayName());
    assertNull(rep.getUrl());
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // update
    rep.url("https://www.example.com/").displayName("Example company").attribute("foo", "bar");
    response = organizationsApi.updateOrganization(REALM, id, rep);
    assertThat(response.getStatus(), is(204));

    // get single
    response = organizationsApi.getOrganizationById(REALM, id);
    rep = response.readEntity(new GenericType<Organization>() {});
    assertThat(response.getStatus(), is(200));
    assertNotNull(rep.getId());
    assertNotNull(rep.getAttributes());
    assertThat(rep.getAttributes().size(), is(1));
    assertThat(rep.getAttributes().get("foo").size(), is(1));
    assertThat(rep.getAttributes().get("foo").get(0), is("bar"));
    assertThat(rep.getDisplayName(), is("Example company"));
    assertThat(rep.getUrl(), is("https://www.example.com/"));
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // delete
    deleteOrg(organizationsApi, REALM, id);

    // get single
    response = organizationsApi.getOrganizationById(REALM, id);
    assertThat(response.getStatus(), is(404));

    // get list
    response = organizationsApi.getOrganizations(REALM, null, 0, 10);
    assertThat(response.getStatus(), is(200));
    orgs = response.readEntity(new GenericType<List<Organization>>() {});
    assertNotNull(orgs);
    assertThat(orgs.size(), is(0));
  }

  @Test
  public void testAddGetDeleteMemberships() {
    Keycloak keycloak = server.client();
    PhaseTwo client = phaseTwo(keycloak);
    OrganizationMembershipsApi membershipsApi = client.members();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    // get empty members list
    response = membershipsApi.getOrganizationMemberships(REALM, id);
    assertThat(response.getStatus(), is(200));
    List<UserRepresentation> members =
        response.readEntity(new GenericType<List<UserRepresentation>>() {});
    assertNotNull(members);
    assertThat(members.size(), is(1)); // org admin default

    // create a user
    UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // check membership before add
    response = membershipsApi.checkOrganizationMembership(REALM, id, user.getId());
    assertThat(response.getStatus(), is(404));

    // add membership
    response = membershipsApi.addOrganizationMember(REALM, id, user.getId());
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getHeaderString("Location"));
    String loc = response.getHeaderString("Location");
    assertThat(loc, is(url(REALM, urlencode(id), "members", user.getId())));
    
    // check membership after add
    response = membershipsApi.checkOrganizationMembership(REALM, id, user.getId());
    assertThat(response.getStatus(), is(204));

    // get members list
    response = membershipsApi.getOrganizationMemberships(REALM, id);
    assertThat(response.getStatus(), is(200));
    members = response.readEntity(new GenericType<List<UserRepresentation>>() {});
    assertNotNull(members);
    assertThat(members.size(), is(2)); // +default org admin
    int idx = 0;
    if (members.get(idx).getUsername().startsWith("org")) idx++;
    assertThat(members.get(idx).getUsername(), is("johndoe"));

    // delete membership
    response = membershipsApi.removeOrganizationMember(REALM, id, user.getId());
    assertThat(response.getStatus(), is(204));

    // check membership after delete
    response = membershipsApi.checkOrganizationMembership(REALM, id, user.getId());
    assertThat(response.getStatus(), is(404));

    // add membership
    response = membershipsApi.addOrganizationMember(REALM, id, user.getId());
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getHeaderString("Location"));
    loc = response.getHeaderString("Location");
    assertThat(loc, is(url(REALM, urlencode(id), "members", user.getId())));

    UsersApi usersApi = client.users();
    response = usersApi.realmUsersUserIdOrgsGet(REALM, user.getId());
    assertThat(response.getStatus(), is(200));
    List<Organization> orgs = response.readEntity(new GenericType<List<Organization>>() {});
    assertNotNull(orgs);
    assertThat(orgs.size(), is(1));
    assertThat(orgs.get(0).getName(), is("example"));

    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // check membership after delete user
    response = membershipsApi.checkOrganizationMembership(REALM, id, user.getId());
    assertThat(response.getStatus(), is(404));

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  public void testDuplicateRoles() {
    PhaseTwo client = phaseTwo();
    OrganizationRolesApi rolesApi = client.roles();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    // get default roles list
    response = rolesApi.getOrganizationRoles(REALM, id);
    assertThat(response.getStatus(), is(200));
    List<OrganizationRole> roles = response.readEntity(new GenericType<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = "eat-apples";
    OrganizationRole roleRep = new OrganizationRole().name(name);
    response = rolesApi.createOrganizationRole(REALM, id, roleRep);
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getHeaderString("Location"));

    // attempt to create same name role
    roleRep = new OrganizationRole().name(name);
    response = rolesApi.createOrganizationRole(REALM, id, roleRep);
    assertThat(response.getStatus(), is(409));

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  public void testAddGetDeleteRoles() {
    Keycloak keycloak = server.client();
    PhaseTwo client = phaseTwo(keycloak);
    OrganizationRolesApi rolesApi = client.roles();
    OrganizationMembershipsApi membershipsApi = client.members();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    // get default roles list
    response = rolesApi.getOrganizationRoles(REALM, id);
    assertThat(response.getStatus(), is(200));
    List<OrganizationRole> roles = response.readEntity(new GenericType<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = createRole(rolesApi, id, "eat-apples");

    // get single role
    response = rolesApi.getOrganizationRole(REALM, id, name);
    assertThat(response.getStatus(), is(200));
    OrganizationRole role = response.readEntity(new GenericType<OrganizationRole>() {});
    assertNotNull(role);
    assertNotNull(role.getId());
    assertThat(role.getName(), is("eat-apples"));

    // get role list
    response = rolesApi.getOrganizationRoles(REALM, id);
    assertThat(response.getStatus(), is(200));
    roles = response.readEntity(new GenericType<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 1));

    // delete role
    response = rolesApi.deleteOrganizationRole(REALM, id, name);
    assertThat(response.getStatus(), is(204));

    // get single role 404
    response = rolesApi.getOrganizationRole(REALM, id, name);
    assertThat(response.getStatus(), is(404));

    // create 3 roles
    String[] ra = {"eat-apples", "bake-pies", "view-fair"};
    for (String r : ra) {
      createRole(rolesApi, id, r);
    }

    // get role list
    response = rolesApi.getOrganizationRoles(REALM, id);
    assertThat(response.getStatus(), is(200));
    roles = response.readEntity(new GenericType<List<OrganizationRole>>() {});
    assertNotNull(roles);
    assertThat(roles.size(), is(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 3));

    // create a user
    UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // add membership
    response = membershipsApi.addOrganizationMember(REALM, id, user.getId());
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getHeaderString("Location"));
    String loc = response.getHeaderString("Location");
    assertThat(loc, is(url(REALM, urlencode(id), "members", user.getId())));

    // grant role to user
    grantUserRole(rolesApi, id, name, user.getId());

    // get users with role
    response = rolesApi.getUserOrganizationRoles(REALM, id, name);
    assertThat(response.getStatus(), is(200));
    List<UserRepresentation> rs = response.readEntity(new GenericType<List<UserRepresentation>>() {});
    assertNotNull(rs);
    assertThat(rs.size(), is(1));
    assertThat(rs.get(0).getUsername(), is("johndoe"));

    // check if user has role
    checkUserRole(rolesApi, id, name, user.getId(), 204);

    // revoke role from user
    response = rolesApi.revokeUserOrganizationRole(REALM, id, name, user.getId());
    assertThat(response.getStatus(), is(204));

    // get users with role
    response = rolesApi.getUserOrganizationRoles(REALM, id, name);
    assertThat(response.getStatus(), is(200));
    rs = response.readEntity(new GenericType<List<UserRepresentation>>() {});
    assertNotNull(rs);
    assertThat(rs.size(), is(0));

    // check if user has role
    checkUserRole(rolesApi, id, name, user.getId(), 404);

    // grant more roles
    for (String r : ra) {
      grantUserRole(rolesApi, id, r, user.getId());
    }

    // check if user has role
    for (String r : ra) {
      checkUserRole(rolesApi, id, r, user.getId(), 204);
    }

    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // get users with role
    for (String r : ra) {
      response = rolesApi.getUserOrganizationRoles(REALM, id, r);
      assertThat(response.getStatus(), is(200));
      rs = response.readEntity(new GenericType<List<UserRepresentation>>() {});
      assertNotNull(rs);
      assertThat(rs.size(), is(0));
    }

    // delete roles
    for (String r : ra) {
      response = rolesApi.deleteOrganizationRole(REALM, id, r);
      assertThat(response.getStatus(), is(204));
    }

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  public void testAddGetDeleteInvitations() {
    Keycloak keycloak = server.client();
    PhaseTwo client = phaseTwo(keycloak);
    OrganizationsApi organizationsApi = client.organizations();
    OrganizationInvitationsApi invitationsApi = client.invitations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    // create invitation
    InvitationRequest inv = new InvitationRequest().email("johndoe@example.com");
    response = invitationsApi.addOrganizationInvitation(REALM, id, inv);
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getHeaderString("Location"));
    String loc = response.getHeaderString("Location");
    String inviteId = loc.substring(loc.lastIndexOf("/") + 1);
    assertNotNull(inviteId);
    assertThat(loc, is(url(REALM, urlencode(id), "invitations", inviteId)));

    // get invitations
    response = invitationsApi.getOrganizationInvitations(REALM, id);
    assertThat(response.getStatus(), is(200));
    List<Invitation> invites = response.readEntity(new GenericType<List<Invitation>>() {});
    assertNotNull(invites);
    assertThat(invites.size(), is(1));
    assertThat(invites.get(0).getEmail(), is("johndoe@example.com"));
    String invId = invites.get(0).getId();

    // try a conflicting invitation
    response = invitationsApi.addOrganizationInvitation(REALM, id, inv);
    assertThat(response.getStatus(), is(409));

    // remove pending invitation
    response = invitationsApi.removeOrganizationInvitation(REALM, id, invId);
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
    user1 = createUser(keycloak, REALM, user1);
    // grant membership to org
    response = client.members().addOrganizationMember(REALM, id, user1.getId());
    assertThat(response.getStatus(), is(201));

    // try an invitation to that new user
    response = invitationsApi.addOrganizationInvitation(REALM, id, inv);
    assertThat(response.getStatus(), is(409));

    // get invitations
    response = invitationsApi.getOrganizationInvitations(REALM, id);
    assertThat(response.getStatus(), is(200));
    invites = response.readEntity(new GenericType<List<Invitation>>() {});
    assertNotNull(invites);
    assertThat(invites.size(), is(0));

    // delete user
    deleteUser(keycloak, REALM, user1.getId());

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  public void testAddGetDeleteIdps() {
    PhaseTwo client = phaseTwo();
    IdentityProvidersApi idpsApi = client.idps();
    OrganizationsApi organizationsApi = client.organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

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
    response = idpsApi.createIdp(REALM, id, idp);
    assertThat(response.getStatus(), is(201));
    String loc = response.getHeaderString("Location");
    String alias1 = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(loc, is(url(REALM, urlencode(id), "idps", alias1)));
    
    // get idps
    response = idpsApi.getIdps(REALM, id);
    assertThat(response.getStatus(), is(200));
    List<IdentityProviderRepresentation> idps =
        response.readEntity(new GenericType<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(1));
    assertTrue(idps.get(0).isEnabled());
    // assertThat(idps.get(0).getAlias(), is("test-oidc-01"));
    assertThat(idps.get(0).getAlias(), is(alias1));
    assertThat(idps.get(0).getProviderId(), is("oidc"));

    // create idp
    idp.setAlias("vendor-protocol-2");
    response = idpsApi.createIdp(REALM, id, idp);
    assertThat(response.getStatus(), is(201));
    loc = response.getHeaderString("Location");
    String alias2 = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(loc, is(url(REALM, urlencode(id), "idps", alias2)));

    // get idps
    response = idpsApi.getIdps(REALM, id);
    assertThat(response.getStatus(), is(200));
    idps = response.readEntity(new GenericType<List<IdentityProviderRepresentation>>() {});
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
    response = idpsApi.getIdpMappers(REALM, id, alias1);
    assertThat(response.getStatus(), is(200));
    List<IdentityProviderMapperRepresentation> mappers =
        response.readEntity(new GenericType<List<IdentityProviderMapperRepresentation>>() {});
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
    response = idpsApi.addIdpMapper(REALM, id, alias1, mapper);
    assertThat(response.getStatus(), is(201));
    loc = response.getHeaderString("Location");
    String mapperId = loc.substring(loc.lastIndexOf("/") + 1);

    // get single mapper for idp
    response = idpsApi.getIdpMapper(REALM, id, alias1, mapperId);
    assertThat(response.getStatus(), is(200));
    mapper = response.readEntity(new GenericType<IdentityProviderMapperRepresentation>() {});
    assertNotNull(mapper);
    assertThat(mapper.getName(), is("name"));
    assertThat(mapper.getIdentityProviderAlias(), is(alias1));
    assertThat(mapper.getIdentityProviderMapper(), is("oidc-user-attribute-idp-mapper"));

    // get mappers for idp
    response = idpsApi.getIdpMappers(REALM, id, alias1);
    assertThat(response.getStatus(), is(200));
    mappers = response.readEntity(new GenericType<List<IdentityProviderMapperRepresentation>>() {});
    assertThat(mappers.size(), is(1));

    // update mapper for idp
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("user.attribute", "lastName")
            .put("claim", "familyName")
            .build());
    response = idpsApi.updateIdpMapper(REALM, id, alias1, mapperId, mapper);
    assertThat(response.getStatus(), is(204));

    // get single mapper for idp
    response = idpsApi.getIdpMapper(REALM, id, alias1, mapperId);
    assertThat(response.getStatus(), is(200));
    mapper = response.readEntity(new GenericType<IdentityProviderMapperRepresentation>() {});
    assertNotNull(mapper);
    assertThat(mapper.getConfig().get("user.attribute"), is("lastName"));
    assertThat(mapper.getConfig().get("claim"), is("familyName"));

    // delete mappers for idp
    response = idpsApi.deleteIdpMapper(REALM, id, alias1, mapperId);
    assertThat(response.getStatus(), is(204));

    // get mappers for idp
    response = idpsApi.getIdpMappers(REALM, id, alias1);
    assertThat(response.getStatus(), is(200));
    mappers = response.readEntity(new GenericType<List<IdentityProviderMapperRepresentation>>() {});
    assertThat(mappers.size(), is(0));

    // delete idps
    response = idpsApi.deleteIdp(REALM, id, alias1);
    assertThat(response.getStatus(), is(204));
    response = idpsApi.deleteIdp(REALM, id, alias2);
    assertThat(response.getStatus(), is(204));

    // get idps
    response = idpsApi.getIdps(REALM, id);
    assertThat(response.getStatus(), is(200));
    idps = response.readEntity(new GenericType<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(0));

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }

  @Test
  @Ignore
  public void testIdpsOwnedOrgs() {
    PhaseTwo client = phaseTwo();
    IdentityProvidersApi idpsApi = client.idps();
    OrganizationsApi organizationsApi = client.organizations();

    // create one org
    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String orgId1 = createOrg(organizationsApi, REALM, rep);

    // create another org
    rep = new Organization().name("sample").domains(ImmutableSet.of("sample.com"));
    String orgId2 = createOrg(organizationsApi, REALM, rep);

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
    response = idpsApi.createIdp(REALM, orgId1, idp);
    assertThat(response.getStatus(), is(201));
    String loc = response.getHeaderString("Location");
    String alias1 = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(loc, is(url(REALM, urlencode(orgId1), "idps", alias1)));

    // create idp for org 2
    idp.setAlias("vendor-protocol-B1");
    response = idpsApi.createIdp(REALM, orgId2, idp);
    assertThat(response.getStatus(), is(201));
    loc = response.getHeaderString("Location");
    String alias2 = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(loc, is(url(REALM, urlencode(orgId2), "idps", alias1)));

    // check that org 1 can only see idp 1
    response = idpsApi.getIdps(REALM, orgId1);
    assertThat(response.getStatus(), is(200));
    List<IdentityProviderRepresentation> idps =
        response.readEntity(new GenericType<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(1));
    assertThat(idps.get(0).getAlias(), is(alias1));

    // check that org 2 can only see idp 2
    response = idpsApi.getIdps(REALM, orgId2);
    assertThat(response.getStatus(), is(200));
    idps = response.readEntity(new GenericType<List<IdentityProviderRepresentation>>() {});
    assertNotNull(idps);
    assertThat(idps.size(), is(1));
    assertThat(idps.get(0).getAlias(), is(alias2));

    // check that org 1 cannot delete/update idp 2
    response = idpsApi.deleteIdp(REALM, orgId1, alias2);
    assertThat(response.getStatus(), not(200));

    // delete idps 1 & 2
    response = idpsApi.deleteIdp(REALM, orgId1, alias1);
    assertThat(response.getStatus(), is(204));
    response = idpsApi.deleteIdp(REALM, orgId2, alias2);
    assertThat(response.getStatus(), is(204));

    // delete org 2
    deleteOrg(organizationsApi, REALM, orgId2);

    // delete org 1
    deleteOrg(organizationsApi, REALM, orgId1);
  }

  @Test
  public void testOrgAdminPermissions() {
    Keycloak keycloak = server.client();
    PhaseTwo client1 = phaseTwo(keycloak);
    OrganizationsApi organizationsApi1 = client1.organizations();
    OrganizationMembershipsApi membershipsApi = client1.members();

    // create one org
    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String orgId1 = createOrg(organizationsApi1, REALM, rep);

    // create a normal user
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType("password");
    pass.setValue("pass");
    pass.setTemporary(false);
    UserRepresentation user1 = new UserRepresentation();
    user1.setEnabled(true);
    user1.setUsername("user1");
    user1.setCredentials(ImmutableList.of(pass));
    user1 = createUser(keycloak, REALM, user1);
    // grant membership to org
    response = membershipsApi.addOrganizationMember(REALM, orgId1, user1.getId());
    assertThat(response.getStatus(), is(201));

    // grant org admin permissions
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      grantUserRole(client1.roles(), orgId1, role, user1.getId());
    }

    Keycloak kc1 = server.client(REALM, "admin-cli", "user1", "pass");
    PhaseTwo client2 = phaseTwo(kc1);
    OrganizationsApi organizationsApi2 = client2.organizations();

    // check that user has permissions to
    //  update org
    rep.url("https://www.example.com/").displayName("Example company");
    response = organizationsApi2.updateOrganization(REALM, orgId1, rep);
    assertThat(response.getStatus(), is(204));
    response = organizationsApi2.getOrganizationById(REALM, orgId1);
    rep = response.readEntity(new GenericType<Organization>() {});
    assertThat(response.getStatus(), is(200));
    assertNotNull(rep.getId());
    assertThat(rep.getAttributes().size(), is(0));
    assertThat(rep.getDisplayName(), is("Example company"));
    assertThat(rep.getUrl(), is("https://www.example.com/"));
    assertThat(rep.getRealm(), is(REALM));
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

    IdentityProvidersApi idpsApi = client2.idps();
    response = idpsApi.createIdp(REALM, orgId1, idp);
    assertThat(response.getStatus(), is(201));
    String loc = response.getHeaderString("Location");
    String alias1 = loc.substring(loc.lastIndexOf("/") + 1);
    //  get idp xxx
    response = idpsApi.getIdp(REALM, orgId1, alias1);
    IdentityProviderRepresentation idp1 = response.readEntity(new GenericType<IdentityProviderRepresentation>() {});
    assertThat(response.getStatus(), is(200));
    assertThat(idp1.getAlias(), is(alias1));
    assertThat(idp1.getProviderId(), is(idp.getProviderId()));
    assertTrue(idp1.isEnabled());
    assertThat(idp1.getFirstBrokerLoginFlowAlias(), is(idp.getFirstBrokerLoginFlowAlias()));
    assertThat(idp1.getConfig().get("clientId"), is(idp.getConfig().get("clientId")));
    //  remove idp
    response = idpsApi.deleteIdp(REALM, orgId1, alias1);
    assertThat(response.getStatus(), is(204));

    // create another org
    rep = new Organization().name("sample").domains(ImmutableSet.of("sample.com"));
    String orgId2 = createOrg(organizationsApi1, REALM, rep);

    // check that user does not have permission to update org
    rep.url("https://www.sample.com/").displayName("Sample company");
    response = organizationsApi2.updateOrganization(REALM, orgId2, rep);
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
    deleteUser(keycloak, REALM, user1.getId());

    // delete other org
    deleteOrg(organizationsApi1, REALM, orgId2);

    // delete org
    deleteOrg(organizationsApi1, REALM, orgId1);
  }

  @Test
  @Ignore
  public void testOrgPortalLink() {
    OrganizationsApi organizationsApi = phaseTwo().organizations();

    Organization rep = new Organization().name("example").domains(ImmutableSet.of("example.com"));
    String id = createOrg(organizationsApi, REALM, rep);

    response = organizationsApi.createPortalLink(REALM, id, "foobar");
    assertThat(response.getStatus(), is(200));
    Map<String, String> resp = response.readEntity(new GenericType<Map<String, String>>() {});
    assertNotNull(resp.get("user"));
    assertNotNull(resp.get("link"));
    assertNotNull(resp.get("redirect"));

    System.err.println(String.format("portal-link %s", resp));

    // delete org
    deleteOrg(organizationsApi, REALM, id);
  }
}
