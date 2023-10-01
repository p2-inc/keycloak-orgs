package io.phasetwo.service.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.OrganizationsResource;
import io.phasetwo.client.PhaseTwo;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.junit.jupiter.Container;

import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractResourceTest {

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
    List<File> dependencies =
        Maven.resolver()
            .loadPomFromFile("./pom.xml")
            .resolve(pkg)
            .withoutTransitivity()
            .asList(File.class);
    return dependencies;
  }

  public static Keycloak keycloak;
  public static ResteasyClient resteasyClient;

  @Container
  public static final KeycloakContainer container =
      new KeycloakContainer("quay.io/phasetwo/keycloak-crdb:22.0.3")
          .withContextPath("/auth")
          .withReuse(true)
          .withProviderClassesFrom("target/classes")
          .withProviderLibsFrom(getDeps());

  @BeforeAll
  public static void beforeAll() {
    container.start();
    resteasyClient = new ResteasyClientBuilderImpl()
            .disableTrustManager()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    keycloak = getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
  }

  @AfterAll
  public static void afterAll() {
    container.stop();
  }

  public static Keycloak getKeycloak() {
    return container.getKeycloakAdminClient();
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    return Keycloak.getInstance(getAuthUrl(), realm, user, pass, clientId);
  }

  public static String getAuthUrl() {
    return container.getAuthServerUrl();
  }

  public static final String REALM = "master";

  public static PhaseTwo phaseTwo() {
    return phaseTwo(getKeycloak());
  }

  public static PhaseTwo phaseTwo(Keycloak keycloak) {
    return new PhaseTwo(keycloak, getAuthUrl());
  }

  protected String createDefaultOrg(OrganizationsResource resource) {
    OrganizationRepresentation rep =
        new OrganizationRepresentation().name("example").domains(List.of("example.com"));
    return resource.create(rep);
  }
  protected Response getRequest(String ...paths) {
    return getRequest(String.join("/", paths), keycloak);
  }
  protected Response getRequest(String path, Keycloak keycloak) {
    return givenSpec(keycloak).when().get(path).then().extract().response();
  }

  protected Response postRequest(Object body, String ...paths) throws JsonProcessingException {
    return postRequest(body, String.join("/", paths), keycloak);
  }

  protected Response postRequest(Object body, String path, Keycloak keycloak) throws JsonProcessingException {
    return givenSpec(keycloak)
            .and()
            .body(toJsonString(body))
            .post(path)
            .then()
            .extract().response();
  }

  protected Response putRequest(Object body, String ...paths) throws JsonProcessingException {
    return putRequest(body, String.join("/", paths), keycloak);
  }

  protected Response putRequest(Object body, String path, Keycloak keycloak) throws JsonProcessingException {
    return givenSpec(keycloak)
            .and()
            .body(toJsonString(body))
            .put(path)
            .then()
            .extract()
            .response();
  }

  protected OrganizationRepresentation createOrganization(OrganizationRepresentation representation) throws IOException {
    return createOrganization(representation, keycloak);
  }

  // create an organization, fet the created organization and returns it
  protected OrganizationRepresentation createOrganization(OrganizationRepresentation representation, Keycloak keycloak) throws IOException {
    Response response = givenSpec(keycloak)
            .and()
            .body(toJsonString(representation))
            .when()
            .post()
            .then()
            .extract()
            .response();

    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));
    String loc = response.getHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);

    response = getRequest(id);
    assertThat(response.statusCode(), Matchers.is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    OrganizationRepresentation orgRep = new ObjectMapper().readValue(response.getBody().asString(), OrganizationRepresentation.class);
    assertThat(orgRep.getId(), is(id));
    return orgRep;
  }

  protected OrganizationRoleRepresentation createOrgRole(String orgId, String name) throws Exception {
    return createOrgRole(orgId, name, keycloak);
  }

  protected OrganizationRoleRepresentation createOrgRole(String orgId, String name, Keycloak keycloak) throws Exception {
    OrganizationRoleRepresentation rep = new OrganizationRoleRepresentation().name(name);
    Response response = givenSpec(keycloak)
            .and()
            .body(toJsonString(rep))
            .when()
            .post(String.join("/", orgId, "roles"))
            .then()
            .extract()
            .response();
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));
    assertNotNull(response.getHeader("Location"));
    String loc = response.getHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);
    assertThat(id, is(name));

    response = getRequest("/%s/roles/%s".formatted(orgId, id));
    assertThat(response.statusCode(), Matchers.is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
    OrganizationRoleRepresentation orgRoleRep = new ObjectMapper().readValue(response.getBody().asString(), OrganizationRoleRepresentation.class);
    assertThat(orgRoleRep.getName(), is(name));
    return orgRoleRep;
  }

  protected void grantUserRole(String orgId, String role, String userId) {
    grantUserRole(orgId, role, userId, keycloak);
  }

  protected void grantUserRole(String orgId, String role, String userId, Keycloak keycloak) {
    // PUT /:realm/orgs/:orgId/roles/:name/users/:userId
    Response response = givenSpec(keycloak)
            .and()
            .body("foo")
            .when()
            .put(String.join("/", orgId, "roles", role, "users", userId))
            .then()
            .extract()
            .response();
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.CREATED.getStatusCode()));
  }

  protected void deleteOrganization(String id) {
    deleteOrganization(id, keycloak);
  }

  protected void deleteOrganization(String id, Keycloak keycloak) {
    Response response = givenSpec(keycloak).when().delete(id).then().extract().response();
    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode()));
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
}
