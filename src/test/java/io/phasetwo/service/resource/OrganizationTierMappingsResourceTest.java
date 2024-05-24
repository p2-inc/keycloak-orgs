package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.deleteUser;
import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.OrganizationTierMapping;
import io.phasetwo.service.representation.OrganizationTier;
import io.phasetwo.service.representation.SwitchOrganization;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.RoleRepresentation;

@JBossLog
class OrganizationTierMappingsResourceTest extends AbstractOrganizationTest  {

  private static final String TESTING_ORG = "test-org";
  private static final String TESTING_ROLE = "test-realm-role";
  private static final String TESTING_PUBLIC_CLIENT = "test-ui";

  private final List<String> roleIds = new ArrayList<>();
  private final List<String> orgIds = new ArrayList<>();
  private String userId;
  private String publicClientName;
  private String clientScopeName;

  @BeforeEach
  void prepareForTest() throws Exception {
    orgIds.add(createOrganization(new OrganizationRepresentation().name(TESTING_ORG)).getId());
    userId = createUserWithCredentials(keycloak, REALM, "user", "password").getId();
  }

  @AfterEach
  void cleanForNextTests() throws Exception {
    orgIds.forEach(this::deleteOrganization);
    orgIds.clear();

    roleIds.forEach(this::deleteRealmRole);
    roleIds.clear();

    deleteUser(keycloak, REALM, userId);

    if(publicClientName != null) {
      deleteClient(publicClientName);
      publicClientName = null;
    }

    if(clientScopeName != null) {
      deleteClientScope(clientScopeName);
      clientScopeName = null;
    }
  }

  @Test
  void platformAdmin_getTierMappings_shouldReturnEmptyArray() throws Exception {
    Response response = getRequest(orgIds.get(0), "role-mappings");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));

    OrganizationTierMapping roleMappings = objectMapper()
        .readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(roleMappings, notNullValue());
    assertThat(roleMappings.getRealmMappings(), notNullValue());
    assertThat(roleMappings.getRealmMappings().isEmpty(), is(true));
  }

  @Test
  void platformAdmin_createAndGetTierMappings_shouldBeSuccessful() throws Exception {
    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "9999-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgIds.get(0), role, expirationDate);
    validateTierMappingsMatchExpected(tierMappings, role, expirationDate);
  }

  @Test
  void platformAdmin_updateTier_shouldChangeExpirationDate() throws Exception {
    String orgId = orgIds.get(0);

    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "3000-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgId, role, expirationDate);
    OrganizationTier tier = tierMappings.getRealmMappings().get(0);

    String newExpirationDate = "3030-01-01";
    tier.setExpireDate(newExpirationDate);
    Response response = putRequest(List.of(tier), orgId, "role-mappings", "realm");
    assertThat(response.statusCode(), is(Status.CREATED.getStatusCode()));
    response = getRequest(orgId, "role-mappings");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));

    tierMappings = objectMapper()
        .readValue(response.getBody().asString(), OrganizationTierMapping.class);
    assertThat(tierMappings.getRealmMappings().size(), is(1));
    tier = tierMappings.getRealmMappings().get(0);
    assertThat(tier.getExpireDate(), is(newExpirationDate));
  }

  @Test
  void platformAdmin_createTierMappings_shouldFailForDateFormat() throws Exception {
    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "01-01-2023T22:22:22";
    ObjectNode body = createTierBody(role, expirationDate);
    Response response = putRequest(List.of(body), orgIds.get(0), "role-mappings", "realm");
    assertThat(response.statusCode(), is(Status.BAD_REQUEST.getStatusCode()));
    Map<String, Object> responseBody = objectMapper()
        .readValue(response.getBody().asString(), new TypeReference<HashMap<String,Object>>() {});
    assertThat(responseBody.get("error_description"), is("Could not parse date format, expected format: yyyy-MM-dd"));
  }

  @Test
  void platformAdmin_createAndGetExpiredTierMappings_shouldBeRemoved() throws Exception {
    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "2000-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgIds.get(0), role, expirationDate);
    assertThat(tierMappings, notNullValue());
    assertThat(tierMappings.getRealmMappings(), notNullValue());
    assertThat(tierMappings.getRealmMappings().isEmpty(), is(true));
  }

  @Test
  void platformAdmin_deleteTierMapping_shouldBeSuccessful() throws Exception {
    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "9999-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgIds.get(0), role, expirationDate);
    validateTierMappingsMatchExpected(tierMappings, role, expirationDate);
    OrganizationTier tier = extractTier(tierMappings, role);

    Response response = deleteRequest(List.of(tier), orgIds.get(0), "role-mappings", "realm");
    assertThat(response.statusCode(), is(204));

    response = getRequest(orgIds.get(0), "role-mappings");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));

    OrganizationTierMapping roleMappings = objectMapper()
        .readValue(response.getBody().asString(), OrganizationTierMapping.class);
    assertThat(roleMappings, notNullValue());
    assertThat(roleMappings.getRealmMappings(), notNullValue());
    assertThat(roleMappings.getRealmMappings().isEmpty(), is(true));
  }

  @Test
  void endUser_getTierMappings_shouldBeOk() throws Exception {
    putRequest("pass", orgIds.get(0), "members", userId);
    Keycloak kc = getKeycloak(REALM, "admin-cli", "user", "password");

    Response response = getRequest(kc,orgIds.get(0) + "/role-mappings");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));
  }

  @Test
  void endUser_createTierMappings_shouldReturnNotAuthorized() throws Exception {
    putRequest("pass", orgIds.get(0), "members", userId);

    String expirationDate = "9999-01-01";
    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    ObjectNode body = createTierBody(role, expirationDate);

    Keycloak kc = getKeycloak(REALM, "admin-cli", "user", "password");
    Response response = putRequest(kc, List.of(body), orgIds.get(0) + "/role-mappings/realm");
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  void endUser_deleteTierMappings_shouldReturnNotAuthorized() throws Exception {
    putRequest("pass", orgIds.get(0), "members", userId);

    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "9999-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgIds.get(0), role, expirationDate);
    validateTierMappingsMatchExpected(tierMappings, role, expirationDate);
    OrganizationTier tier = extractTier(tierMappings, role);

    Keycloak kc = getKeycloak(REALM, "admin-cli", "user", "password");
    Response response = deleteRequest(kc, List.of(tier), orgIds.get(0) + "/role-mappings/realm");
    assertThat(response.statusCode(), is(Status.UNAUTHORIZED.getStatusCode()));
  }

  @Test
  void endUser_accessToken_shouldContainsTiers_withActiveOrganizationTierScopeMapperAndOtherRoles() throws Exception {
    // Validate that tiers assigned to the organizations are added to the roles without overriding others
    publicClientName = TESTING_PUBLIC_CLIENT;
    clientScopeName = "active_organization_tiers";
    putRequest("pass", orgIds.get(0), "members", userId);

    createPublicClient(publicClientName);
    createActiveOrganizationTierScopeMapper(clientScopeName, publicClientName);

    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "9999-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgIds.get(0), role, expirationDate);
    validateTierMappingsMatchExpected(tierMappings, role, expirationDate);

    String realmRoleName = "great-realm-role";
    RoleRepresentation realmRole = createAndReturnTestRealmRole(realmRoleName);
    grantUserRealmRole(userId, List.of(realmRole));

    Keycloak kc = getKeycloak(REALM, publicClientName, "user", "password");
    AccessToken decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getRealmAccess().getRoles().contains(TESTING_ROLE), is(true));
    assertThat(decodedToken.getRealmAccess().getRoles().contains(realmRoleName), is(true));
  }

  @Test
  void endUser_multiOrg_accessToken_shouldContainsActiveOrgTiers_withActiveOrganizationTierScopeMapper() throws Exception {
    publicClientName = TESTING_PUBLIC_CLIENT;
    createPublicClient(publicClientName);
    String expirationDate = "9999-01-01";

    String firstOrgId = orgIds.get(0);
    putRequest("pass", firstOrgId, "members", userId);
    RoleRepresentation firstRole = createAndReturnTestRealmRole(TESTING_ROLE);
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(firstOrgId, firstRole, expirationDate);
    assertThat(tierMappings.getRealmMappings().size(), is(1));


    String secondOrgId = createOrganization(new OrganizationRepresentation().name("second-test-org")).getId();
    orgIds.add(secondOrgId);
    putRequest("pass", secondOrgId, "members", userId);
    String secondOrgRole = "org-2-role";
    RoleRepresentation secondRole = createAndReturnTestRealmRole(secondOrgRole);
    tierMappings = createAndReturnTierMappings(secondOrgId, secondRole, expirationDate);
    assertThat(tierMappings.getRealmMappings().size(), is(1));

    clientScopeName = "active_organization_tiers";
    createActiveOrganizationTierScopeMapper(clientScopeName, publicClientName);

    Keycloak kc = getKeycloak(REALM, publicClientName, "user", "password");
    // test org 1 context token roles
    Response response = putRequest(kc, new SwitchOrganization().id(firstOrgId), "users", "switch-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    kc = getKeycloak(REALM, publicClientName, "user", "password");
    AccessToken decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getRealmAccess().getRoles().contains(firstRole.getName()), is(true));
    assertThat(decodedToken.getRealmAccess().getRoles().contains(secondRole.getName()), is(false));

    // test org 2 context token roles
    response = putRequest(kc, new SwitchOrganization().id(secondOrgId), "users", "switch-organization");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    kc = getKeycloak(REALM, publicClientName, "user", "password");
    decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getRealmAccess().getRoles().contains(firstRole.getName()), is(false));
    assertThat(decodedToken.getRealmAccess().getRoles().contains(secondRole.getName()), is(true));
  }

  @Test
  void endUser_accessToken_shouldNotContainsExpiredTiers_withActiveOrganizationTierScopeMapper() throws Exception {
    publicClientName = TESTING_PUBLIC_CLIENT;
    clientScopeName = "active_organization_tiers";
    putRequest("pass", orgIds.get(0), "members", userId);

    createPublicClient(publicClientName);
    createActiveOrganizationTierScopeMapper(clientScopeName, publicClientName);

    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "2000-01-01";
    createAndReturnTierMappings(orgIds.get(0), role, expirationDate);

    Keycloak kc = getKeycloak(REALM, publicClientName, "user", "password");
    AccessToken decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getRealmAccess().getRoles().contains(TESTING_ROLE), is(false));
  }

  @Test
  void endUser_accessToken_claimsShouldContainsTiers_withActiveOrganizationMapper() throws Exception {
    publicClientName = TESTING_PUBLIC_CLIENT;
    clientScopeName = "active_organization";
    putRequest("pass", orgIds.get(0), "members", userId);

    createPublicClient(publicClientName);
    createActiveOrganizationScopeMapper(clientScopeName, "id, name, role, attribute, tiers", publicClientName);

    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "9999-01-01";
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(orgIds.get(0), role, expirationDate);
    validateTierMappingsMatchExpected(tierMappings, role, expirationDate);

    Keycloak kc = getKeycloak(REALM, publicClientName, "user", "password");
    AccessToken decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getOtherClaims().isEmpty(), is(false));
    assertThat(decodedToken.getOtherClaims().containsKey("active_organization"), is(true));
    Map<String, Object> activeOrgClaims = (Map<String, Object>) decodedToken.getOtherClaims().get("active_organization");
    assertThat(activeOrgClaims.containsKey("tiers"), is(true));
    assertThat(((List<String>) activeOrgClaims.get("tiers")).contains(TESTING_ROLE), is(true));
  }

  @Test
  void endUser_accessToken_claimsShouldNotContainsExpiredTiers_withActiveOrganizationMapper() throws Exception {
    publicClientName = TESTING_PUBLIC_CLIENT;
    clientScopeName = "active_organization";
    putRequest("pass", orgIds.get(0), "members", userId);

    createPublicClient(publicClientName);
    createActiveOrganizationScopeMapper(clientScopeName, "id, name, role, attribute, tiers", publicClientName);

    RoleRepresentation role = createAndReturnTestRealmRole(TESTING_ROLE);
    String expirationDate = "2000-01-01";
    createAndReturnTierMappings(orgIds.get(0), role, expirationDate);

    Keycloak kc = getKeycloak(REALM, publicClientName, "user", "password");
    AccessToken decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getOtherClaims().isEmpty(), is(false));
    assertThat(decodedToken.getOtherClaims().containsKey("active_organization"), is(true));
    Map<String, Object> activeOrgClaims = (Map<String, Object>) decodedToken.getOtherClaims().get("active_organization");
    assertThat(activeOrgClaims.containsKey("tiers"), is(true));
    assertThat(((List<String>) activeOrgClaims.get("tiers")).contains(TESTING_ROLE), is(false));
  }

  @Test
  void endUser_multiOrg_accessToken_shouldContainsAllTiers_withOrganizationTierMapper() throws Exception {
    publicClientName = TESTING_PUBLIC_CLIENT;
    createPublicClient(publicClientName);

    String firstOrgId = orgIds.get(0);
    putRequest("pass", firstOrgId, "members", userId);

    String secondOrgId = createOrganization(new OrganizationRepresentation().name("second-test-org")).getId();
    orgIds.add(secondOrgId);
    putRequest("pass", secondOrgId, "members", userId);

    clientScopeName = "organization_tiers";
    createOrganizationTierMapper(clientScopeName, publicClientName);

    String expirationDate = "9999-01-01";
    RoleRepresentation firstRole = createAndReturnTestRealmRole(TESTING_ROLE);
    OrganizationTierMapping tierMappings = createAndReturnTierMappings(firstOrgId, firstRole, expirationDate);
    assertThat(tierMappings.getRealmMappings().size(), is(1));

    String secondOrgRole = "org-2-role";
    RoleRepresentation secondRole = createAndReturnTestRealmRole(secondOrgRole);
    tierMappings = createAndReturnTierMappings(secondOrgId, secondRole, expirationDate);
    assertThat(tierMappings.getRealmMappings().size(), is(1));

    Keycloak kc = getKeycloak(REALM, publicClientName, "user", "password");
    AccessToken decodedToken =
        TokenVerifier.create(kc.tokenManager().getAccessTokenString(), AccessToken.class).getToken();
    assertThat(decodedToken.getRealmAccess().getRoles().contains(TESTING_ROLE), is(true));
    assertThat(decodedToken.getRealmAccess().getRoles().contains(secondOrgRole), is(true));
  }

  private RoleRepresentation createAndReturnTestRealmRole(String roleName) throws JsonProcessingException {
    ObjectNode body = objectMapper().createObjectNode();
    body.put("name", roleName);
    body.put("description", "Test realm role");
    body.putIfAbsent("attributes", objectMapper().createObjectNode());
    RoleRepresentation role = createAndReturnRealmRole(body);
    roleIds.add(role.getId());
    return role;
  }

  private OrganizationTierMapping createAndReturnTierMappings(
      String orgId, RoleRepresentation role, String expirationDate) throws Exception {

    ObjectNode body = createTierBody(role, expirationDate);
    Response response = putRequest(List.of(body), orgId, "role-mappings", "realm");
    assertThat(response.statusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "role-mappings");
    assertThat(response.statusCode(), is(Status.OK.getStatusCode()));

    OrganizationTierMapping roleMappings = objectMapper()
        .readValue(response.getBody().asString(), OrganizationTierMapping.class);

    return roleMappings;
  }

  private ObjectNode createTierBody(RoleRepresentation role, String expirationDate) {
    ObjectNode roleObject = objectMapper().createObjectNode();
    roleObject.put("id", role.getId());
    roleObject.put("name", role.getName());
    ObjectNode body = objectMapper().createObjectNode();
    body.putIfAbsent("role", roleObject);
    body.put("expireDate", expirationDate);
    return body;
  }

  private void validateTierMappingsMatchExpected(
      OrganizationTierMapping tierMappings, RoleRepresentation role, String expirationDate) {
    assertThat(tierMappings, notNullValue());
    assertThat(tierMappings.getRealmMappings(), notNullValue());
    assertThat(tierMappings.getRealmMappings().isEmpty(), is(false));
    assertThat(tierMappings.getRealmMappings().size(), is(1));
    OrganizationTier tier = tierMappings.getRealmMappings().get(0);
    assertThat(tier.getRole().getName(), is(role.getName()));
    assertThat(tier.getExpireDate(), is(expirationDate));
  }

  private OrganizationTier extractTier(OrganizationTierMapping tierMapping, RoleRepresentation role) {
    return tierMapping.getRealmMappings()
        .stream()
        .filter(t -> t.getRole().getId().equals(role.getId()))
        .findFirst()
        .orElse(null);
  }
}
