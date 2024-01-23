package io.phasetwo.service;

import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.service.resource.OrganizationResourceProviderFactory;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matchers;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.junit.jupiter.Container;

public abstract class AbstractOrganizationTest {

  public static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/phasetwo/keycloak-crdb:%s", System.getProperty("keycloak-version", "23.0.0"));
  public static final String ADMIN_CLI = "admin-cli";
  static final String[] deps = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "org.keycloak:keycloak-admin-client"
  };

  static List<File> getDeps() {
    List<File> dependencies = new ArrayList<File>();
    for (String dep : deps) {
      dependencies.addAll(getDep(dep));
    }
    return dependencies;
  }

  static List<File> getDep(String pkg) {
    return Maven.resolver()
        .loadPomFromFile("./pom.xml")
        .resolve(pkg)
        .withoutTransitivity()
        .asList(File.class);
  }

  public static Keycloak keycloak;
  public static ResteasyClient resteasyClient;

  @Container
  public static final KeycloakContainer container =
      new KeycloakContainer(KEYCLOAK_IMAGE)
          .withContextPath("/auth")
          .withReuse(true)
          .withProviderClassesFrom("target/classes")
          .withProviderLibsFrom(getDeps());

  @BeforeAll
  public static void beforeAll() {
    container.start();
    resteasyClient =
        new ResteasyClientBuilderImpl()
            .disableTrustManager()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    keycloak =
        getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
  }

  @AfterAll
  public static void afterAll() {
    container.stop();
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    return Keycloak.getInstance(getAuthUrl(), realm, user, pass, clientId);
  }

  public static String getAuthUrl() {
    return container.getAuthServerUrl();
  }

  public static final String REALM = "master";

  protected OrganizationRepresentation createDefaultOrg() throws IOException {
    OrganizationRepresentation rep =
        new OrganizationRepresentation().name("example").domains(List.of("example.com"));
    return createOrganization(rep);
  }

  protected Response getRequest(String... paths) {
    return getRequest(keycloak, String.join("/", paths));
  }

  protected Response getRequest(Keycloak keycloak, String path) {
    return givenSpec(keycloak).when().get(path).andReturn();
  }

  protected Response postRequest(Object body, String... paths) throws JsonProcessingException {
    return postRequest(keycloak, body, String.join("/", paths));
  }

  protected Response postRequest(Keycloak keycloak, Object body, String path)
      throws JsonProcessingException {
    return givenSpec(keycloak)
        .and()
        .body(toJsonString(body))
        .post(path)
        .then()
        .extract()
        .response();
  }

  protected Response putRequest(Object body, String... paths) throws JsonProcessingException {
    return putRequest(keycloak, body, String.join("/", paths));
  }

  protected Response putRequest(Keycloak keycloak, Object body, String path)
      throws JsonProcessingException {
    return givenSpec(keycloak).and().body(toJsonString(body)).put(path).then().extract().response();
  }

  protected Response deleteRequest(String... paths) {
    return deleteRequest(keycloak, String.join("/", paths));
  }

  protected Response deleteRequest(Keycloak keycloak, String path) {
    return givenSpec(keycloak).when().delete(path).then().extract().response();
  }

  protected OrganizationRepresentation createOrganization(OrganizationRepresentation representation)
      throws IOException {
    return createOrganization(keycloak, representation);
  }

  // create an organization, fet the created organization and returns it
  protected OrganizationRepresentation createOrganization(
      Keycloak keycloak, OrganizationRepresentation representation) throws IOException {
    Response response =
        givenSpec(keycloak).and().body(toJsonString(representation)).when().post().andReturn();

    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));
    String loc = response.getHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);

    response = getRequest(id);
    assertThat(response.statusCode(), Matchers.is(Status.OK.getStatusCode()));
    OrganizationRepresentation orgRep =
        new ObjectMapper()
            .readValue(response.getBody().asString(), OrganizationRepresentation.class);
    assertThat(orgRep.getId(), is(id));
    return orgRep;
  }

  protected OrganizationRoleRepresentation createOrgRole(String orgId, String name)
      throws IOException {
    return createOrgRole(keycloak, orgId, name);
  }

  protected OrganizationRoleRepresentation createOrgRole(
      Keycloak keycloak, String orgId, String name) throws IOException {
    OrganizationRoleRepresentation rep = new OrganizationRoleRepresentation().name(name);
    Response response =
        givenSpec(keycloak)
            .and()
            .body(toJsonString(rep))
            .when()
            .post(String.join("/", orgId, "roles"))
            .andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));
    String loc = response.getHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(id, is(name));

    response = getRequest("/%s/roles/%s".formatted(orgId, id));
    assertThat(response.statusCode(), Matchers.is(Status.OK.getStatusCode()));
    OrganizationRoleRepresentation orgRoleRep =
        new ObjectMapper()
            .readValue(response.getBody().asString(), OrganizationRoleRepresentation.class);
    assertThat(orgRoleRep.getName(), is(name));
    return orgRoleRep;
  }

  protected void grantUserRole(String orgId, String role, String userId) {
    grantUserRole(keycloak, orgId, role, userId);
  }

  protected void grantUserRole(Keycloak keycloak, String orgId, String role, String userId) {
    // PUT /:realm/orgs/:orgId/roles/:name/users/:userId
    Response response =
        givenSpec(keycloak)
            .and()
            .body("foo")
            .when()
            .put(String.join("/", orgId, "roles", role, "users", userId))
            .then()
            .extract()
            .response();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
  }

  protected void deleteOrganization(String id) {
    deleteOrganization(keycloak, id);
  }

  protected void deleteOrganization(Keycloak keycloak, String id) {
    Response response = givenSpec(keycloak).when().delete(id).then().extract().response();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
  }

  protected RequestSpecification givenSpec(String... paths) {
    return givenSpec(keycloak, paths);
  }

  protected RequestSpecification givenSpec(Keycloak keycloak, String... paths) {
    if (paths.length > 0) {
      return given()
          .baseUri(container.getAuthServerUrl())
          .basePath("realms/" + REALM + "/" + String.join("/", paths))
          .contentType("application/json")
          .auth()
          .oauth2(keycloak.tokenManager().getAccessTokenString());
    }
    return given()
        .baseUri(container.getAuthServerUrl())
        .basePath("realms/" + REALM + "/" + OrganizationResourceProviderFactory.ID)
        .contentType("application/json")
        .auth()
        .oauth2(keycloak.tokenManager().getAccessTokenString());
  }

  protected void checkUserRole(String orgId, String role, String userId, int status) {
    // check if user has role
    Response response =
        givenSpec().when().get(String.join("/", orgId, "roles", role, "users", userId)).andReturn();
    assertThat(response.getStatusCode(), is(status));
  }
}
