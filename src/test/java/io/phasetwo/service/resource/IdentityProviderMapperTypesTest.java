package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.deleteUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableMap;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * Regression tests for the identity-provider mapper privilege-escalation issue: a tenant admin
 * holding only the org-scoped {@code manage-identity-providers} role could add a stock role-importer
 * mapper (e.g. {@code oidc-hardcoded-role-idp-mapper} granting {@code realm-management.realm-admin})
 * to their organization's identity provider, escalating the next brokered user to realm admin and
 * gaining cross-organization access.
 */
@JBossLog
public class IdentityProviderMapperTypesTest extends AbstractOrganizationTest {

  private static final String ADMIN_CLI = "admin-cli";

  private final List<String> createdOrgIds = new ArrayList<>();
  private final List<String> createdUserIds = new ArrayList<>();

  @AfterEach
  void cleanup() {
    // Best-effort teardown so a failing assertion cannot leak an organization (whose name/domain
    // would then collide with the next test) or its admin user.
    for (String orgId : createdOrgIds) {
      try {
        deleteOrganization(orgId);
      } catch (Exception e) {
        log.warnf("could not clean up organization %s: %s", orgId, e.getMessage());
      }
    }
    for (String userId : createdUserIds) {
      try {
        deleteUser(keycloak, REALM, userId);
      } catch (Exception e) {
        log.warnf("could not clean up user %s: %s", userId, e.getMessage());
      }
    }
    createdOrgIds.clear();
    createdUserIds.clear();
  }

  private String newOrg(String slug) throws IOException {
    OrganizationRepresentation rep =
        new OrganizationRepresentation()
            .name("idp-esc-" + slug)
            .domains(List.of(slug + ".example.com"));
    String orgId = createOrganization(rep).getId();
    createdOrgIds.add(orgId);
    return orgId;
  }

  private IdentityProviderRepresentation oidcIdp(String alias) {
    IdentityProviderRepresentation idp = new IdentityProviderRepresentation();
    idp.setAlias(alias);
    idp.setProviderId("oidc");
    idp.setEnabled(true);
    idp.setFirstBrokerLoginFlowAlias("first broker login");
    idp.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("useJwksUrl", "true")
            .put("syncMode", "FORCE")
            .put("authorizationUrl", "https://foo.com")
            .put("tokenUrl", "https://foo.com")
            .put("clientAuthMethod", "client_secret_post")
            .put("clientId", "aabbcc")
            .put("clientSecret", "112233")
            .build());
    return idp;
  }

  private IdentityProviderMapperRepresentation hardcodedRealmAdminMapper(String alias) {
    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setName("escalate");
    mapper.setIdentityProviderAlias(alias);
    mapper.setIdentityProviderMapper("oidc-hardcoded-role-idp-mapper");
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("role", "realm-management.realm-admin")
            .build());
    return mapper;
  }

  private Keycloak orgIdpAdmin(String orgId, String username) throws IOException {
    UserRepresentation user = createUserWithCredentials(keycloak, REALM, username, "pass");
    createdUserIds.add(user.getId());
    putRequest("foo", orgId, "members", user.getId());
    // A stock org admin holds both IdP roles (both are in DEFAULT_ORG_ROLES); view is required to
    // reach the idps sub-resource, manage is the role under test.
    grantUserRole(orgId, OrganizationAdminAuth.ORG_ROLE_VIEW_IDENTITY_PROVIDERS, user.getId());
    grantUserRole(orgId, OrganizationAdminAuth.ORG_ROLE_MANAGE_IDENTITY_PROVIDERS, user.getId());
    return getKeycloak(REALM, ADMIN_CLI, username, "pass");
  }

  @Test
  void orgAdminCannotAssignRealmRoleViaIdpMapper() throws IOException {
    var orgId = newOrg("role");
    Keycloak orgAdmin = orgIdpAdmin(orgId, "idpadmin-role");

    // The delegated org admin legitimately creates an IdP for their org
    String alias = "tenant-idp";
    var createResp = postRequest(orgAdmin, oidcIdp(alias), "%s/idps".formatted(orgId));
    assertThat(createResp.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));

    // ...but must not be able to add a mapper that grants a realm-management client role
    var attack =
        postRequest(
            orgAdmin,
            hardcodedRealmAdminMapper(alias),
            "%s/idps/%s/mappers".formatted(orgId, alias));
    assertThat(attack.getStatusCode(), is(Response.Status.FORBIDDEN.getStatusCode()));
  }

  @Test
  void orgAdminCannotUseNonAllowlistedMapper() throws IOException {
    // Deny by default: the guard is an allowlist, so even a mapper type that is neither a role nor
    // a
    // group importer (here a hardcoded attribute mapper) is rejected for a delegated org admin
    // unless it is explicitly allowlisted.
    var orgId = newOrg("other");
    Keycloak orgAdmin = orgIdpAdmin(orgId, "idpadmin-other");

    String alias = "tenant-idp-other";
    postRequest(orgAdmin, oidcIdp(alias), "%s/idps".formatted(orgId));

    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setName("hardcoded-attr");
    mapper.setIdentityProviderAlias(alias);
    mapper.setIdentityProviderMapper("hardcoded-attribute-idp-mapper");
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("attribute", "some-attr")
            .put("attribute.value", "some-value")
            .build());
    var attack = postRequest(orgAdmin, mapper, "%s/idps/%s/mappers".formatted(orgId, alias));
    assertThat(attack.getStatusCode(), is(Response.Status.FORBIDDEN.getStatusCode()));
  }

  @Test
  void orgAdminCannotAssignGroupViaIdpMapper() throws IOException {
    // Group membership is the same class of vector: a group can carry privileged composite roles or
    // cross-org access, so a delegated org admin must not be able to add a group-importer mapper.
    var orgId = newOrg("group");
    Keycloak orgAdmin = orgIdpAdmin(orgId, "idpadmin-group");

    String alias = "tenant-idp-group";
    postRequest(orgAdmin, oidcIdp(alias), "%s/idps".formatted(orgId));

    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setName("join-group");
    mapper.setIdentityProviderAlias(alias);
    mapper.setIdentityProviderMapper("oidc-hardcoded-group-idp-mapper");
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("group", "/administrators")
            .build());
    var attack = postRequest(orgAdmin, mapper, "%s/idps/%s/mappers".formatted(orgId, alias));
    assertThat(attack.getStatusCode(), is(Response.Status.FORBIDDEN.getStatusCode()));
  }

  @Test
  void orgAdminCanUseAllowlistedAttributeMapper() throws IOException {
    // Positive control: an allowlisted attribute mapper is still permitted.
    var orgId = newOrg("ok");
    Keycloak orgAdmin = orgIdpAdmin(orgId, "idpadmin-ok");

    String alias = "tenant-idp-ok";
    postRequest(orgAdmin, oidcIdp(alias), "%s/idps".formatted(orgId));

    IdentityProviderMapperRepresentation mapper = new IdentityProviderMapperRepresentation();
    mapper.setName("email-attr");
    mapper.setIdentityProviderAlias(alias);
    mapper.setIdentityProviderMapper("oidc-user-attribute-idp-mapper");
    mapper.setConfig(
        new ImmutableMap.Builder<String, String>()
            .put("syncMode", "INHERIT")
            .put("claim", "email")
            .put("user.attribute", "email")
            .build());
    var resp = postRequest(orgAdmin, mapper, "%s/idps/%s/mappers".formatted(orgId, alias));
    assertThat(resp.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));
  }

  @Test
  void realmAdminCanStillAssignRoleViaIdpMapper() throws IOException {
    // Backward compatibility: a full realm admin (manage-organizations) is unaffected.
    var orgId = newOrg("admin");

    String alias = "admin-idp";
    postRequest(oidcIdp(alias), "%s/idps".formatted(orgId));

    var resp =
        postRequest(hardcodedRealmAdminMapper(alias), "%s/idps/%s/mappers".formatted(orgId, alias));
    assertThat(resp.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));
  }
}
