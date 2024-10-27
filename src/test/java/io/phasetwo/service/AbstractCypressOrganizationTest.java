package io.phasetwo.service;

import static io.phasetwo.service.Helpers.enableEvents;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Helpers.toJsonString;
import static io.phasetwo.service.Orgs.ORG_BROWSER_AUTH_FLOW_ALIAS;
import static io.phasetwo.service.Orgs.ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
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
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.Testcontainers;

public class AbstractCypressOrganizationTest {

  protected static final boolean RUN_CYPRESS =
      Boolean.parseBoolean(System.getProperty("include.cypress", "false"));

  public static final String KEYCLOAK_IMAGE =
          String.format(
            "quay.io/phasetwo/keycloak-crdb:%s", System.getProperty("keycloak-version", "26.0.2"));
  public static final String REALM = "master";
  public static final String ADMIN_CLI = "admin-cli";

  static final String[] deps = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "io.phasetwo.keycloak:keycloak-events"
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

  public static final KeycloakContainer container =
      new KeycloakContainer(KEYCLOAK_IMAGE)
          .withContextPath("/auth")
          .withReuse(true)
          .withProviderClassesFrom("target/classes")
          .withProviderLibsFrom(getDeps())
          .withAccessToHost(true);

  protected static final int WEBHOOK_SERVER_PORT = 8083;

  static {
    container.start();
  }

  @BeforeAll
  public static void beforeAll() {
    if (!RUN_CYPRESS) {
      return; // do nothing
    }

    Testcontainers.exposeHostPorts(WEBHOOK_SERVER_PORT);
    resteasyClient =
        new ResteasyClientBuilderImpl()
            .disableTrustManager()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    keycloak =
        getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
    enableEvents(keycloak, "master");
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    return Keycloak.getInstance(getAuthUrl(), realm, user, pass, clientId);
  }

  public static String getAuthUrl() {
    return container.getAuthServerUrl();
  }

  protected Response getRequest(String... paths) {
    return getRequest(keycloak, String.join("/", paths));
  }

  protected Response getRequest(Keycloak keycloak, String path) {
    return givenSpec(keycloak).when().get(path).andReturn();
  }

  protected Response putRequest(Object body, String... paths) throws JsonProcessingException {
    return putRequest(keycloak, body, String.join("/", paths));
  }

  protected Response putRequest(Keycloak keycloak, Object body, String path)
      throws JsonProcessingException {
    return givenSpec(keycloak).and().body(toJsonString(body)).put(path).then().extract().response();
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
        objectMapper().readValue(response.getBody().asString(), OrganizationRepresentation.class);
    assertThat(orgRep.getId(), is(id));
    return orgRep;
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

  protected void grantClientRoles(String clientName, String userId, String... roleNames)
      throws JsonProcessingException {
    ObjectMapper mapper = objectMapper();

    // build root RequestSpecification
    RequestSpecification root = getAdminRootRequest();

    // get client
    String clientId = getClientId(clientName);

    // get client roles
    Response response =
        root.when().get("clients/" + clientId + "/roles").then().extract().response();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    ArrayNode roleArrayNode = (ArrayNode) mapper.readTree(response.getBody().asString());
    ArrayNode arrayBody = mapper.createArrayNode();
    for (JsonNode roleJsonNode : roleArrayNode) {
      for (String roleName : roleNames) {
        if (roleJsonNode.get("name").asText().equals(roleName)) {
          ObjectNode body = mapper.createObjectNode();
          body.put("id", roleJsonNode.get("id").asText());
          body.put("name", roleJsonNode.get("name").asText());
          body.put("description", roleJsonNode.get("description").asText());
          arrayBody.add(body);
          break;
        }
      }
    }

    // assign client role to user
    response =
        root.and()
            .body(arrayBody)
            .when()
            .post("users/" + userId + "/role-mappings/clients/" + clientId)
            .then()
            .extract()
            .response();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
  }

  protected void createPublicClient(String clientId) {
    ObjectMapper mapper = objectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.put("protocol", "openid-connect");
    body.put("clientId", clientId);
    body.put("name", clientId);
    body.put("description", "");
    body.put("publicClient", true);
    body.put("authorizationServicesEnabled", false);
    body.put("serviceAccountsEnabled", false);
    body.put("implicitFlowEnabled", false);
    body.put("directAccessGrantsEnabled", true);
    body.put("standardFlowEnabled", true);
    body.put("frontchannelLogout", true);
    body.put("alwaysDisplayInConsole", false);
    body.put("rootUrl", "http://localhost:3000");
    body.put("baseUrl", "http://localhost:3000");
    body.putIfAbsent("redirectUris", mapper.createArrayNode().add("*"));
    body.putIfAbsent("webOrigins", mapper.createArrayNode().add("*"));

    ObjectNode attributes = mapper.createObjectNode();
    attributes.put("saml_idp_initiated_sso_url_name", "");
    attributes.put("oauth2.device.authorization.grant.enabled", false);
    attributes.put("oidc.ciba.grant.enabled", false);
    body.putIfAbsent("attributes", attributes);

    Response response = getAdminRootRequest().body(body).post("clients").andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
  }

  private String getClientId(String clientName) throws JsonProcessingException {
    // get clients
    Response response = getAdminRootRequest().when().get("clients?first=0&max=20").andReturn();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    return getElementId(response, "clientId", clientName);
  }

  private String getElementId(Response response, String targetKey, String targetValue)
      throws JsonProcessingException {
    ArrayNode clientArrayNode = (ArrayNode) objectMapper().readTree(response.getBody().asString());
    String id = "";
    for (JsonNode clientJsonNode : clientArrayNode) {
      if (clientJsonNode.get(targetKey).asText().equals(targetValue)) {
        id = clientJsonNode.get("id").asText();
        break;
      }
    }
    assertThat(id, is(not("")));

    return id;
  }

  private RequestSpecification getAdminRootRequest() {
    return given()
        .baseUri(container.getAuthServerUrl())
        .basePath("/admin/realms/" + REALM + "/")
        .contentType("application/json")
        .auth()
        .oauth2(keycloak.tokenManager().getAccessTokenString());
  }

  protected void configureSelectOrgFlows() throws JsonProcessingException {
    ObjectMapper mapper = objectMapper();
    RequestSpecification root = getAdminRootRequest();

    Response response = root.when().get().then().extract().response();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    JsonNode realm = mapper.readTree(response.getBody().asString());
    ((ObjectNode) realm).put("browserFlow", ORG_BROWSER_AUTH_FLOW_ALIAS);
    ((ObjectNode) realm).put("directGrantFlow", ORG_DIRECT_GRANT_AUTH_FLOW_ALIAS);

    response = root.and().body(realm).when().put().then().extract().response();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
  }
}
