package io.phasetwo.service;

import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import io.phasetwo.service.representation.OrganizationRole;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.List;
import org.keycloak.admin.client.Keycloak;

public class KeycloakOrgsAdminAPI {

  private static final String ORGS_SEGMENT = "orgs";

  private final String authServerUrl;
  private final String realm;
  private final Keycloak adminClient;

  public KeycloakOrgsAdminAPI(String authServerUrl, String realm, Keycloak adminClient) {
    this.authServerUrl = authServerUrl;
    this.realm = realm;
    this.adminClient = adminClient;
  }

  private RequestSpecification givenSpec() {
    return given()
        .baseUri(authServerUrl)
        .basePath("realms/" + realm + "/" + ORGS_SEGMENT)
        .contentType("application/json")
        .auth()
        .oauth2(adminClient.tokenManager().getAccessTokenString());
  }

  /** Creates an organization and returns its full representation. */
  public OrganizationRepresentation createOrganization(OrganizationRepresentation representation)
      throws JsonProcessingException {
    Response response =
        givenSpec().body(toJsonString(representation)).post().andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    String loc = response.getHeader("Location");
    assertNotNull(loc);
    String id = loc.substring(loc.lastIndexOf("/") + 1);
    response = givenSpec().get(id).andReturn();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    OrganizationRepresentation orgRep =
        objectMapper().readValue(response.getBody().asString(), OrganizationRepresentation.class);
    assertThat(orgRep.getId(), is(id));
    return orgRep;
  }

  /** Returns the names of all roles defined on an organization. */
  public List<String> getOrgRoleNames(String orgId) throws JsonProcessingException {
    Response response = givenSpec().get(orgId + "/roles").andReturn();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    List<OrganizationRoleRepresentation> roles =
        objectMapper()
            .readValue(
                response.getBody().asString(),
                new TypeReference<List<OrganizationRoleRepresentation>>() {});
    return roles.stream().map(OrganizationRoleRepresentation::getName).toList();
  }

  public void assertOrgHasRole(String orgId, String roleName) throws JsonProcessingException {
    List<String> names = getOrgRoleNames(orgId);
    assertThat(
        "org " + orgId + " should have role " + roleName, names.contains(roleName), is(true));
  }

  /** Returns all organizations in the realm. */
  public List<OrganizationRepresentation> listOrganizations() throws JsonProcessingException {
    Response response = givenSpec().get().andReturn();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
      return objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
  }

  /** Returns all roles defined on an organization. */
  public List<OrganizationRole> getOrgRoles(String orgId) throws JsonProcessingException {
    Response response = givenSpec().get(orgId + "/roles").andReturn();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));
    return objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
  }

  /** Exports all organizations (with optional member/invitation data). */
  public KeycloakOrgsRepresentation exportOrgs(boolean exportMembersAndInvitations)
      throws JsonProcessingException {
    Response response =
        givenSpec()
            .queryParam("exportMembersAndInvitations", exportMembersAndInvitations)
            .get("export")
            .andReturn();
    return objectMapper()
        .readValue(response.getBody().asString(), KeycloakOrgsRepresentation.class);
  }

  /** Imports organizations, returning the raw response. */
  public Response importOrgs(
      KeycloakOrgsRepresentation representation,
      boolean skipMissingMember,
      boolean skipMissingIdp) {
    return givenSpec()
        .queryParam("skipMissingMember", skipMissingMember)
        .queryParam("skipMissingIdp", skipMissingIdp)
        .body(representation)
        .post("import")
        .andReturn();
  }
}
