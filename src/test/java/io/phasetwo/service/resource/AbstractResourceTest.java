package io.phasetwo.service.resource;

import io.phasetwo.service.KeycloakSuite;
import io.phasetwo.service.openapi.PhaseTwo;
import io.phasetwo.service.openapi.api.OrganizationRolesApi;
import io.phasetwo.service.openapi.api.OrganizationsApi;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationRole;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.ClassRule;
import org.keycloak.admin.client.Keycloak;

import javax.ws.rs.core.Response;

import static io.phasetwo.service.Helpers.urlencode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractResourceTest {

  public static final String REALM = "master";

  @ClassRule
  public static KeycloakSuite server = KeycloakSuite.SERVER;

  public static PhaseTwo phaseTwo() {
    return phaseTwo(server.client());
  }
  public static PhaseTwo phaseTwo(Keycloak keycloak) {
    return new PhaseTwo(keycloak, server.getAuthUrl());
  }

  protected String url(String realm, String... segments) {
    StringBuilder o = new StringBuilder();
    for (String segment : segments) {
      o.append("/").append(segment);
    }
    return String.format("%s/realms/%s/orgs%s", server.getAuthUrl(), realm, o);
  }

  protected String createRole(OrganizationRolesApi api, String orgId, String name) {
    OrganizationRole rep = new OrganizationRole().name(name);
    try(Response response = api.createOrganizationRole(REALM, orgId, rep)) {
      assertThat(response.getStatus(), is(201));
      assertNotNull(response.getHeaderString("Location"));
      String loc = response.getHeaderString("Location");
      String id = loc.substring(loc.lastIndexOf("/") + 1);
      assertThat(name, is(id));
      assertThat(loc, is(url(REALM, urlencode(orgId), "roles", id)));
      return id;
    }
  }

  protected void grantUserRole(OrganizationRolesApi api, String orgId, String role, String userId) {
    try(Response response = api.grantUserOrganizationRole(REALM, orgId, role, userId)) {
      assertThat(response.getStatus(), is(201));
      assertNotNull(response.getHeaderString("Location"));
      String loc = response.getHeaderString("Location");
      assertThat(loc, is(url(REALM, urlencode(orgId), "roles", urlencode(role), "users", userId)));
    }
  }

  protected void checkUserRole(OrganizationRolesApi api, String orgId, String role, String userId, int status) {
    // check if user has role
    try(Response response = api.checkUserOrganizationRole(REALM, orgId, role, userId)) {
      assertThat(response.getStatus(), is(status));
    }
  }

  protected String createOrg(OrganizationsApi api, String realm, Organization rep) {
    try(Response response = api.createOrganization(realm, rep)) {
      assertThat(response.getStatus(), is(201));
      assertNotNull(response.getHeaderString("Location"));
      String loc = response.getHeaderString("Location");
      String id = loc.substring(loc.lastIndexOf("/") + 1);
      assertNotNull(id);
      assertThat(loc, is(url(realm, id)));
      return id;
    }
  }

  protected void deleteOrg(OrganizationsApi api, String realm, String orgId) {
    try(Response response = api.deleteOrganization(realm, orgId)) {
      assertThat(response.getStatus(), is(204));
    }
  }
}
