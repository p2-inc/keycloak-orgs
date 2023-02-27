package io.phasetwo.service.resource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.OrganizationDomainsResource;
import io.phasetwo.client.OrganizationIdentityProvidersResource;
import io.phasetwo.client.OrganizationInvitationsResource;
import io.phasetwo.client.OrganizationMembershipsResource;
import io.phasetwo.client.OrganizationResource;
import io.phasetwo.client.OrganizationRolesResource;
import io.phasetwo.client.OrganizationsResource;
import io.phasetwo.client.PhaseTwo;
import io.phasetwo.client.openapi.api.IdentityProvidersApi;
import io.phasetwo.client.openapi.api.UsersApi;
import io.phasetwo.client.openapi.model.IdentityProviderMapperRepresentation;
import io.phasetwo.client.openapi.model.IdentityProviderRepresentation;
import io.phasetwo.client.openapi.model.InvitationRepresentation;
import io.phasetwo.client.openapi.model.InvitationRequestRepresentation;
import io.phasetwo.client.openapi.model.OrganizationDomainRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.client.openapi.model.PortalLinkRepresentation;
import io.phasetwo.client.openapi.model.UserRepresentation;
import lombok.extern.jbosslog.JBossLog;
import org.junit.Ignore;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.phasetwo.service.Helpers.createUser;
import static io.phasetwo.service.Helpers.deleteUser;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.Assert.assertThrows;

@JBossLog
public class OrganizationResourceTest extends AbstractResourceTest {

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
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);
    OrganizationResource organizationResource = organizationsResource.organization(id);
    OrganizationDomainsResource domainsResource = organizationResource.domains();

    List<OrganizationDomainRepresentation> domains = domainsResource.get();
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
    organizationResource.update(organizationResource.get().domains(List.of("foo.com", "bar.net")));

    domains = domainsResource.get();
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
    domainsResource.verify("foo.com");

    // delete org
    organizationResource.delete();
  }

  @Test
  public void testImportConfig() {
    PhaseTwo client = phaseTwo();
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);
    IdentityProvidersApi idpsApi = client.getIdentityProvidersApi();

    // import-config
    Map<String, Object> urlConf =
        ImmutableMap.of(
            "fromUrl",
                "https://login.microsoftonline.com/74df8381-4935-4fa8-8634-8e3413f93086/federationmetadata/2007-06/federationmetadata.xml?appid=ba149e64-4512-440b-a1b4-ae976d85f1ec",
            "providerId", "saml",
            "realm", REALM);
    Map<String, Object> config = idpsApi.importIdpJson(REALM, id, urlConf);
    assertThat(config, notNullValue());

    // delete org
    organizationsResource.organization(id).delete();
  }

  @Test
  public void testAddGetUpdateDeleteOrg() {
    PhaseTwo client = phaseTwo();
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = organizationsResource.create(
            new OrganizationRepresentation().name("example").domains(List.of("example.com"))
    );
    OrganizationResource organizationResource = organizationsResource.organization(id);

    // get single
    OrganizationRepresentation rep = organizationResource.get();
    assertThat(rep, notNullValue());
    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getDisplayName(), nullValue());
    assertThat(rep.getUrl(), nullValue());
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // get list
    List<OrganizationRepresentation> organizations = organizationsResource.get();
    assertThat(organizations, notNullValue());
    assertThat(organizations, hasSize(1));
    rep = organizations.get(0);
    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getDisplayName(), nullValue());
    assertThat(rep.getUrl(), nullValue());
    assertThat(rep.getRealm(), is(REALM));
    assertThat(rep.getDomains().iterator().next(), is("example.com"));
    assertThat(rep.getName(), is("example"));
    assertThat(rep.getId(), is(id));

    // update
    rep.url("https://www.example.com/").displayName("Example company").attributes(ImmutableMap.of("foo", List.of("bar")));
    organizationResource.update(rep);

    // get single
    rep = organizationResource.get();
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
    organizationResource.delete();

    // get single
    assertThrows(ClientErrorException.class, organizationResource::get);

    // get list
    organizations = organizationsResource.get();
    assertThat(organizations, notNullValue());
    assertThat(organizations, empty());
  }

  @Test
  public void testAddGetDeleteMemberships() {
    Keycloak keycloak = server.client();
    PhaseTwo client = phaseTwo(keycloak);
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);

    OrganizationResource organizationResource = organizationsResource.organization(id);
    OrganizationMembershipsResource membershipsResource = organizationResource.memberships();

    // get empty members list
    List<UserRepresentation> members = membershipsResource.members();
    assertThat(members, notNullValue());
    assertThat(members, hasSize(1)); // org admin default

    // create a user
    org.keycloak.representations.idm.UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // check membership before add
    assertThat(membershipsResource.isMember(user.getId()), is(false));

    // add membership and check
    membershipsResource.add(user.getId());
    assertThat(membershipsResource.isMember(user.getId()), is(true));

    // get members list
    members = membershipsResource.members();
    assertThat(members, notNullValue());
    assertThat(members, hasSize(2)); // +default org admin
    int idx = 0;
    if (members.get(idx).getUsername().startsWith("org")) idx++;
    assertThat(members.get(idx).getUsername(), is("johndoe"));

    // delete membership and check
    membershipsResource.remove(user.getId());
    assertThat(membershipsResource.isMember(user.getId()), is(false));

    // add membership
    membershipsResource.add(user.getId());

    UsersApi usersApi = client.getUsersApi();
    List<OrganizationRepresentation> representations = usersApi.realmUsersUserIdOrgsGet(REALM, user.getId());
    assertThat(representations, notNullValue());
    assertThat(representations, hasSize(1));
    assertThat(representations.get(0).getName(), is("example"));

    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // check membership after delete user
    assertThat(membershipsResource.isMember(user.getId()), is(false));

    // delete org
    organizationResource.delete();
  }

  @Test
  public void testDuplicateRoles() {
    PhaseTwo client = phaseTwo();
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);

    OrganizationRolesResource rolesResource = organizationsResource.organization(id).roles();

    // get default roles list
    List<OrganizationRoleRepresentation> roles = rolesResource.get();
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = "eat-apples";
    OrganizationRoleRepresentation roleRep = new OrganizationRoleRepresentation().name(name);
    assertThat(rolesResource.create(roleRep), notNullValue());

    // attempt to create same name role
    assertThrows(ClientErrorException.class, () -> rolesResource.create(
            new OrganizationRoleRepresentation().name(name)));

    // delete org
    organizationsResource.organization(id).delete();
  }

  @Test
  public void testAddGetDeleteRoles() {
    Keycloak keycloak = server.client();
    PhaseTwo client = phaseTwo(keycloak);
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);

    OrganizationResource organizationResource = organizationsResource.organization(id);
    OrganizationRolesResource rolesResource = organizationResource.roles();
    OrganizationMembershipsResource membershipsResource = organizationResource.memberships();

    // get default roles list
    List<OrganizationRoleRepresentation> roles = rolesResource.get();
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length));

    // create a role
    String name = rolesResource.create(new OrganizationRoleRepresentation().name("eat-apples"));

    // get single role
    OrganizationRoleRepresentation role = rolesResource.get(name);
    assertThat(role, notNullValue());
    assertThat(role.getId(), notNullValue());
    assertThat(role.getName(), is("eat-apples"));

    // get role list
    roles = rolesResource.get();
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 1));

    // delete role
    rolesResource.delete(name);

    // get single role 404
    assertThrows(NotFoundException.class, () -> rolesResource.get(name));

    // create 3 roles
    String[] additionalRoles = {"eat-apples", "bake-pies", "view-fair"};
    for (String roleName : additionalRoles) {
      rolesResource.create(new OrganizationRoleRepresentation().name(roleName));
    }

    // get role list
    roles = rolesResource.get();
    assertThat(roles, notNullValue());
    assertThat(roles, hasSize(OrganizationAdminAuth.DEFAULT_ORG_ROLES.length + 3));

    // create a user
    org.keycloak.representations.idm.UserRepresentation user = createUser(keycloak, REALM, "johndoe");

    // add membership
    membershipsResource.add(user.getId());

    // grant role to user
    rolesResource.grant(name, user.getId());

    // get users with role
    List<UserRepresentation> rs = rolesResource.users(name);
    assertThat(rs, notNullValue());
    assertThat(rs, hasSize(1));
    assertThat(rs.get(0).getUsername(), is("johndoe"));

    // check if user has role
    assertThat(rolesResource.hasRole(name, user.getId()), is(true));

    // revoke role from user
    rolesResource.revoke(name, user.getId());

    // get users with role
    rs = rolesResource.users(name);
    assertThat(rs, notNullValue());
    assertThat(rs, hasSize(0));

    // check if user has role
    assertThat(rolesResource.hasRole(name, user.getId()), is(false));

    // grant more roles
    for (String roleName : additionalRoles) {
      rolesResource.grant(roleName, user.getId());
    }

    // check if user has role
    for (String roleName : additionalRoles) {
      assertThat(rolesResource.hasRole(roleName, user.getId()), is(true));
    }

    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // get users with role
    for (String roleName : additionalRoles) {
      rs = rolesResource.users(roleName);
      assertThat(rs, notNullValue());
      assertThat(rs, hasSize(0));
    }

    // delete roles
    for (String roleName : additionalRoles) {
      rolesResource.delete(roleName);
    }

    // delete org
    organizationResource.delete();
  }

  @Test
  public void testAddGetDeleteInvitations() {
    Keycloak keycloak = server.client();
    PhaseTwo client = phaseTwo(keycloak);
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);

    OrganizationResource organizationResource = organizationsResource.organization(id);
    OrganizationInvitationsResource invitationsResource = organizationResource.invitations();

    // create invitation
    InvitationRequestRepresentation inv = new InvitationRequestRepresentation().email("johndoe@example.com");
    String inviteId = invitationsResource.add(inv);
    assertThat(inviteId, notNullValue());

    // get invitations
    List<InvitationRepresentation> invites = invitationsResource.get();
    assertThat(invites, notNullValue());
    assertThat(invites, hasSize(1));
    assertThat(invites.get(0).getEmail(), is("johndoe@example.com"));
    String invId = invites.get(0).getId();

    // try a conflicting invitation
    assertThrows(ClientErrorException.class, () -> invitationsResource.add(inv));

    // remove pending invitation
    invitationsResource.delete(invId);

    // create user and give membership
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType("password");
    pass.setValue("pass");
    pass.setTemporary(false);
    org.keycloak.representations.idm.UserRepresentation user1 =
            new org.keycloak.representations.idm.UserRepresentation();
    user1.setEnabled(true);
    user1.setUsername("user1");
    user1.setEmail("johndoe@example.com");
    user1.setCredentials(ImmutableList.of(pass));
    user1 = createUser(keycloak, REALM, user1);
    // grant membership to org
    organizationResource.memberships().add(user1.getId());

    // try an invitation to that new user
    assertThrows(ClientErrorException.class, () -> invitationsResource.add(inv));

    // get invitations
    invites = invitationsResource.get();
    assertThat(invites, notNullValue());
    assertThat(invites, hasSize(0));

    // delete user
    deleteUser(keycloak, REALM, user1.getId());

    // delete org
    organizationResource.delete();
  }

  @Test
  public void testAddGetDeleteIdps() {
    PhaseTwo client = phaseTwo();
    OrganizationsResource organizationsResource = client.organizations(REALM);
    String id = createDefaultOrg(organizationsResource);

    OrganizationResource organizationResource = organizationsResource.organization(id);
    OrganizationIdentityProvidersResource idpResource = organizationResource.identityProviders();

    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias("vendor-protocol-1");
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
    String alias1 = idpResource.create(idp);;
    assertThat(alias1, notNullValue());
    
    // get idps
    List<IdentityProviderRepresentation> idps = idpResource.get();
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));

    IdentityProviderRepresentation representation = idps.get(0);
    assertThat(representation.getEnabled(), is(true));
    assertThat(representation.getAlias(), is(alias1));
    assertThat(representation.getProviderId(), is("oidc"));

    // create idp
    representation.setAlias("vendor-protocol-2");
    representation.setInternalId(null);
    String alias2 = idpResource.create(representation);
    assertThat(alias2, notNullValue());

    // get idps
    idps = idpResource.get();
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(2));
    for (IdentityProviderRepresentation i : idps) {
      assertThat(i.getEnabled(), is(i.getAlias().equals(alias2)));
    }

    // get mappers for idp
    List<IdentityProviderMapperRepresentation> mappers = idpResource.getMappers(alias1);
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
    String mapperId = idpResource.addMapper(alias1, mapper);
    assertThat(mapperId, notNullValue());

    // get single mapper for idp
    mapper = idpResource.getMapper(alias1, mapperId);
    assertThat(mapper, notNullValue());
    assertThat(mapper.getName(), is("name"));
    assertThat(mapper.getIdentityProviderAlias(), is(alias1));
    assertThat(mapper.getIdentityProviderMapper(), is("oidc-user-attribute-idp-mapper"));

    // get mappers for idp
    mappers = idpResource.getMappers(alias1);
    assertThat(mappers, hasSize(1));

    // update mapper for idp
    mapper.setConfig(
        new ImmutableMap.Builder<String, Object>()
            .put("syncMode", "INHERIT")
            .put("user.attribute", "lastName")
            .put("claim", "familyName")
            .build());
    idpResource.updateMapper(alias1, mapperId, mapper);

    // get single mapper for idp
    mapper = idpResource.getMapper(alias1, mapperId);
    assertThat(mapper, notNullValue());
    assertThat(mapper.getConfig().get("user.attribute"), is("lastName"));
    assertThat(mapper.getConfig().get("claim"), is("familyName"));

    // delete mappers for idp
    idpResource.deleteMapper(alias1, mapperId);

    // get mappers for idp
    mappers = idpResource.getMappers(alias1);
    assertThat(mappers, empty());

    // delete idps
    idpResource.delete(alias1);
    idpResource.delete(alias2);

    // get idps
    idps = idpResource.get();
    assertThat(idps, notNullValue());
    assertThat(idps, empty());

    // delete org
    organizationResource.delete();
  }

  @Test
  @Ignore
  public void testIdpsOwnedOrgs() {
    PhaseTwo client = phaseTwo();
    OrganizationsResource organizationsResource = client.organizations(REALM);

    String orgId1 = organizationsResource.create(new OrganizationRepresentation()
            .name("example")
            .domains(List.of("example.com")));
    String orgId2 = organizationsResource.create(new OrganizationRepresentation()
            .name("sample")
            .domains(List.of("sample.com")));

    OrganizationIdentityProvidersResource firstIdpResource =
            organizationsResource.organization(orgId1).identityProviders();
    OrganizationIdentityProvidersResource secondIdpResource =
            organizationsResource.organization(orgId2).identityProviders();

    // create idp for org 1
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias("vendor-protocol-A1");
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
    String alias1 = firstIdpResource.create(idp);
    assertThat(alias1, notNullValue());

    // create idp for org 2
    idp.setAlias("vendor-protocol-B1");
    String alias2 = secondIdpResource.create(idp);
    assertThat(alias2, notNullValue());

    // check that org 1 can only see idp 1
    List<IdentityProviderRepresentation> idps = firstIdpResource.get();
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));
    assertThat(idps.get(0).getAlias(), is(alias1));

    // check that org 2 can only see idp 2
    idps = secondIdpResource.get();
    assertThat(idps, notNullValue());
    assertThat(idps, hasSize(1));
    assertThat(idps.get(0).getAlias(), is(alias2));

    // check that org 1 cannot delete/update idp 2
    firstIdpResource.delete(alias2);

    // delete idps 1 & 2
    firstIdpResource.delete(alias1);
    secondIdpResource.delete(alias2);

    // delete orgs
    organizationsResource.organization(orgId1).delete();
    organizationsResource.organization(orgId2).delete();
  }

  @Test
  public void testOrgAdminPermissions() {
    Keycloak keycloak = server.client();
    PhaseTwo client1 = phaseTwo(keycloak);
    OrganizationsResource organizationsResource = client1.organizations(REALM);
    String orgId1 = createDefaultOrg(organizationsResource);

    OrganizationResource organizationResource = organizationsResource.organization(orgId1);
    OrganizationMembershipsResource membershipsResource = organizationResource.memberships();
    OrganizationRolesResource rolesResource = organizationResource.roles();

    // create a normal user
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType("password");
    pass.setValue("pass");
    pass.setTemporary(false);
    org.keycloak.representations.idm.UserRepresentation user1 = new org.keycloak.representations.idm.UserRepresentation();
    user1.setEnabled(true);
    user1.setUsername("user1");
    user1.setCredentials(ImmutableList.of(pass));
    user1 = createUser(keycloak, REALM, user1);
    // grant membership to org
    membershipsResource.add(user1.getId());

    // grant org admin permissions
    for (String role : OrganizationAdminAuth.DEFAULT_ORG_ROLES) {
      rolesResource.grant(role, user1.getId());
    }

    Keycloak kc1 = server.client(REALM, "admin-cli", "user1", "pass");
    PhaseTwo client2 = phaseTwo(kc1);
    OrganizationsResource secondOrganizationsResource = client2.organizations(REALM);
    OrganizationResource organizationResourceFromSecondClient = secondOrganizationsResource.organization(orgId1);

    // check that user has permissions to update org
    OrganizationRepresentation rep = organizationResourceFromSecondClient.get();
    rep.url("https://www.example.com/").displayName("Example company");
    organizationResourceFromSecondClient.update(rep);

    rep = organizationResourceFromSecondClient.get();
    assertThat(rep.getId(), notNullValue());
    assertThat(rep.getAttributes().keySet(), empty());
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

    OrganizationIdentityProvidersResource idpResource = organizationResourceFromSecondClient.identityProviders();
    String alias1 = idpResource.create(idp);
    assertThat(alias1, notNullValue());

    //  get idp xxx
    IdentityProviderRepresentation idp1 = idpResource.get(alias1);
    assertThat(idp1, notNullValue());
    assertThat(idp1.getAlias(), is(alias1));
    assertThat(idp1.getProviderId(), is(idp.getProviderId()));
    assertThat(idp1.getEnabled(), is(true));
    assertThat(idp1.getFirstBrokerLoginFlowAlias(), is(idp.getFirstBrokerLoginFlowAlias()));
    assertThat(idp1.getConfig().get("clientId"), is(idp.getConfig().get("clientId")));

    //  remove idp
    idpResource.delete(alias1);

    // create another org
    rep = new OrganizationRepresentation().name("sample").domains(List.of("sample.com"));
    String orgId2 = organizationsResource.create(rep);

    // check that user does not have permission to update org
    rep.url("https://www.sample.com/").displayName("Sample company");
    assertThrows(NotAuthorizedException.class, () -> secondOrganizationsResource.organization(orgId2).get());

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

    // delete orgs
    organizationResource.delete();
    organizationsResource.organization(orgId2).delete();
  }

  @Test
  @Ignore
  public void testOrgPortalLink() {
    OrganizationsResource organizationsResource = phaseTwo().organizations(REALM);
    String id = createDefaultOrg(organizationsResource);

    OrganizationResource organizationResource = organizationsResource.organization(id);
    PortalLinkRepresentation link = organizationResource.portalLink(Optional.of("foobar"));
    assertThat(link, notNullValue());
    assertThat(link.getUser(), notNullValue());
    assertThat(link.getLink(), notNullValue());
    assertThat(link.getRedirect(), notNullValue());

    System.err.println(String.format("portal-link %s", link));

    // delete org
    organizationResource.delete();
  }
}
