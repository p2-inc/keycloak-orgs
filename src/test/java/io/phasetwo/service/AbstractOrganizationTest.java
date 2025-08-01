package io.phasetwo.service;

import static io.phasetwo.service.Helpers.enableEvents;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.OrganizationRoleRepresentation;
import io.phasetwo.service.importexport.representation.InvitationRepresentation;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import io.phasetwo.service.importexport.representation.UserRolesRepresentation;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.OrganizationRole;
import io.phasetwo.service.resource.OrganizationResourceProviderFactory;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;

public abstract class AbstractOrganizationTest {

  public static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/phasetwo/keycloak-crdb:%s", System.getProperty("keycloak-version", "26.2.0"));
  public static final String REALM = "master";
  public static final String ADMIN_CLI = "admin-cli";

  static final String[] deps = {
    "dnsjava:dnsjava",
    "org.wildfly.client:wildfly-client-config",
    "org.jboss.resteasy:resteasy-client",
    "org.jboss.resteasy:resteasy-client-api",
    "org.keycloak:keycloak-admin-client",
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
          .withImagePullPolicy(org.testcontainers.images.PullPolicy.alwaysPull())
          .withContextPath("/auth")
          .withReuse(true)
          .withProviderClassesFrom("target/classes")
          .withExposedPorts(8787, 9000, 8080)
          .withProviderLibsFrom(getDeps())
          .withEnv("JAVA_OPTS", "-agentlib:jdwp=transport=dt_socket,address=*:8787,server=y,suspend=n -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m ")
          .withAccessToHost(true);

  protected static final int WEBHOOK_SERVER_PORT = 8083;

  static {
    container.start();
  }

  @BeforeAll
  public static void beforeAll() {
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

  protected Response getRequest(Keycloak keycloak, String... paths) {
    return givenSpec(keycloak, paths).when().get().andReturn();
  }

  protected Response getRequestFromRoot(Keycloak keycloak, String... paths) {
    return getRootRequest(Optional.of(keycloak)).when().get(String.join("/", paths)).andReturn();
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

  protected Response patchRequest(Object body, String path) throws JsonProcessingException {
    return givenSpec(keycloak)
        .and()
        .body(toJsonString(body))
        .patch(path)
        .then()
        .extract()
        .response();
  }

  protected Response putRequest(Keycloak keycloak, Object body, String... paths)
      throws JsonProcessingException {
    return givenSpec(keycloak, String.join("/", paths))
        .and()
        .body(toJsonString(body))
        .put()
        .then()
        .extract()
        .response();
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
        objectMapper().readValue(response.getBody().asString(), OrganizationRepresentation.class);
    assertThat(orgRep.getId(), is(id));
    return orgRep;
  }

  protected KeycloakOrgsRepresentation exportOrgs(
      Keycloak keycloak, boolean exportMembersAndInvitations) throws IOException {
    var response =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + REALM + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .queryParam("exportMembersAndInvitations", exportMembersAndInvitations)
            .when()
            .get("export")
            .then()
            .extract()
            .response();

    return objectMapper()
        .readValue(response.getBody().asString(), KeycloakOrgsRepresentation.class);
  }

  protected Response importOrgs(
      KeycloakOrgsRepresentation representation, Keycloak keycloak, String realm) {
    return given()
        .baseUri(container.getAuthServerUrl())
        .basePath("realms/" + realm + "/orgs")
        .contentType("application/json")
        .auth()
        .oauth2(keycloak.tokenManager().getAccessTokenString())
        .queryParam("skipMissingMember", false)
        .queryParam("skipMissingIdp", false)
        .when()
        .and()
        .body(representation)
        .when()
        .post("import")
        .then()
        .extract()
        .response();
  }

  protected Response importOrgsSkipMissingMembers(
      KeycloakOrgsRepresentation representation, Keycloak keycloak, String realm) {
    return given()
        .baseUri(container.getAuthServerUrl())
        .basePath("realms/" + realm + "/orgs")
        .contentType("application/json")
        .auth()
        .oauth2(keycloak.tokenManager().getAccessTokenString())
        .queryParam("skipMissingMember", true)
        .queryParam("skipMissingIdp", false)
        .when()
        .and()
        .body(representation)
        .when()
        .post("import")
        .then()
        .extract()
        .response();
  }

  protected Response importOrgSkipMissingIdp(
      KeycloakOrgsRepresentation representation, Keycloak keycloak, String realm) {
    return given()
        .baseUri(container.getAuthServerUrl())
        .basePath("realms/" + realm + "/orgs")
        .contentType("application/json")
        .auth()
        .oauth2(keycloak.tokenManager().getAccessTokenString())
        .queryParam("skipMissingMember", false)
        .queryParam("skipMissingIdp", true)
        .when()
        .and()
        .body(representation)
        .when()
        .post("import")
        .then()
        .extract()
        .response();
  }

  protected void importRealm(RealmRepresentation representation, Keycloak keycloak) {
    var response =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("admin/realms/")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .body(representation)
            .when()
            .post()
            .then()
            .extract()
            .response();
    assertThat(response.getStatusCode(), CoreMatchers.is(Status.CREATED.getStatusCode()));
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
        objectMapper()
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

  protected void revokeUserRole(String orgId, String role, String userId) {
    // Delete /:realm/orgs/:orgId/roles/:name/users/:userId
    Response response =
        givenSpec(keycloak)
            .and()
            .body("foo")
            .when()
            .delete(String.join("/", orgId, "roles", role, "users", userId))
            .then()
            .extract()
            .response();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
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

  protected void grantClientRoles(String clientName, String userId, String... roleNames)
      throws JsonProcessingException {
    ObjectMapper mapper = objectMapper();

    // build root RequestSpecification
    RequestSpecification root = getAdminRootRequest(Optional.empty());

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

    Response response =
        getAdminRootRequest(Optional.empty()).body(body).post("clients").andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
  }

  protected void deleteClient(String clientName) throws JsonProcessingException {
    String clientId = getClientId(clientName);
    Response response =
        getAdminRootRequest(Optional.empty()).delete("clients/" + clientId).andReturn();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
  }

  protected void createClientScope(String clientScopeName) {
    ObjectMapper mapper = objectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.put("name", clientScopeName);
    body.put("description", clientScopeName);
    body.put("type", "none");
    body.put("protocol", "openid-connect");

    ObjectNode attributes = mapper.createObjectNode();
    attributes.put("consent.screen.text", "");
    attributes.put("display.on.consent.screen", false);
    attributes.put("include.in.token.scope", true);
    attributes.put("gui.order", "");
    body.putIfAbsent("attributes", attributes);

    Response response =
        getAdminRootRequest(Optional.empty()).body(body).post("client-scopes").andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
  }

  protected void deleteClientScope(String clientName) throws JsonProcessingException {
    String clientScopeId = getClientScopeId(clientName);
    Response response =
        getAdminRootRequest(Optional.empty()).delete("client-scopes/" + clientScopeId).andReturn();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
  }

  protected void addMapperToClientScope(
      String clientScopeName,
      String claimName,
      String claimType,
      String protocolMapperName,
      Map<String, String> additionalConfig)
      throws JsonProcessingException {
    ObjectMapper mapper = objectMapper();
    ObjectNode body = mapper.createObjectNode();
    body.put("protocol", "openid-connect");
    body.put("protocolMapper", protocolMapperName);
    body.put("name", claimName);

    ObjectNode config = mapper.createObjectNode();
    config.put("claim.name", claimName);
    config.put("jsonType.label", claimType);
    config.put("id.token.claim", true);
    config.put("access.token.claim", true);
    config.put("userinfo.token.claim", true);
    additionalConfig.forEach(config::put);
    body.putIfAbsent("config", config);

    // Search target client-scope
    String clientScopeId = getClientScopeId(clientScopeName);

    // Assign mapper to client-scope
    Response response =
        getAdminRootRequest(Optional.empty())
            .body(body)
            .post("client-scopes/" + clientScopeId + "/protocol-mappers/models")
            .andReturn();
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
  }

  protected void addClientScopeToClient(String clientScopeName, String clientName)
      throws JsonProcessingException {
    // get client
    String clientId = getClientId(clientName);

    // get client scopes
    String clientScopeId = getClientScopeId(clientScopeName);

    Response response =
        getAdminRootRequest(Optional.empty())
            .put("clients/" + clientId + "/default-client-scopes/" + clientScopeId)
            .andReturn();
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));
  }

  protected Response getUserAccount(Keycloak keycloak) {
    Response response = getRequestFromRoot(keycloak, "account?userProfileMetadata=false");
    assertThat(response.getStatusCode(), Matchers.is(Status.OK.getStatusCode()));
    return response;
  }

  protected Response getUser(String userId) {
    Response response =
        getAdminRootRequest(Optional.empty())
            .when()
            .get("/users/" + userId + "?userProfileMetadata=false")
            .andReturn();
    ;
    assertThat(response.getStatusCode(), Matchers.is(Status.OK.getStatusCode()));
    return response;
  }

  protected void createOrReplaceUserAttribute(
      Keycloak keycloak, String username, String attributeKey, String attributeValue)
      throws JsonProcessingException {
    Response response = getUserAccount(keycloak);

    ObjectMapper mapper = objectMapper();
    JsonNode rootNode = mapper.readTree(response.body().asString());
    JsonNode attributeNode = rootNode.get("attributes");

    if (attributeNode == null) {
      attributeNode = mapper.createObjectNode();
    }

    if (attributeNode.has(attributeKey)) {
      ((ObjectNode) attributeNode).remove(attributeKey);
    }

    ((ObjectNode) attributeNode)
        .set(attributeKey, objectMapper().createArrayNode().add(attributeValue));

    ObjectNode body = mapper.createObjectNode();
    body.put("username", username);
    body.set("attributes", attributeNode.deepCopy());

    response = getRootRequest(Optional.of(keycloak)).body(body).post("account").andReturn();
    assertThat(response.getStatusCode(), Matchers.is(Status.NO_CONTENT.getStatusCode()));
  }

  protected void createOrReplaceReadOnlyUserAttribute(
      Keycloak keycloak, String username, String attributeKey, String attributeValue)
      throws JsonProcessingException {
    Response response = getUserAccount(keycloak);

    ObjectMapper mapper = new ObjectMapper();
    JsonNode rootNode = mapper.readTree(response.body().asString());
    JsonNode attributeNode = rootNode.get("attributes");

    if (attributeNode == null) {
      attributeNode = mapper.createObjectNode();
    }

    if (attributeNode.has(attributeKey)) {
      ((ObjectNode) attributeNode).remove(attributeKey);
    }

    ((ObjectNode) attributeNode)
        .set(attributeKey, new ObjectMapper().createArrayNode().add(attributeValue));

    ObjectNode body = mapper.createObjectNode();
    body.put("username", username);
    body.set("attributes", attributeNode.deepCopy());

    response = getRootRequest(Optional.of(keycloak)).body(body).post("account").andReturn();
    assertThat(response.getStatusCode(), Matchers.is(Status.BAD_REQUEST.getStatusCode()));
  }

  private String getClientScopeId(String clientScopeName) throws JsonProcessingException {
    Response response = getAdminRootRequest(Optional.empty()).get("client-scopes").andReturn();
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    return getElementId(response, "name", clientScopeName);
  }

  private String getClientId(String clientName) throws JsonProcessingException {
    // get clients
    Response response =
        getAdminRootRequest(Optional.empty()).when().get("clients?first=0&max=20").andReturn();
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

  private RequestSpecification getAdminRootRequest(Optional<Keycloak> optionalKeycloak) {
    RequestSpecification requestSpecification =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("/admin/realms/" + REALM + "/")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString());

    optionalKeycloak.ifPresent(
        value -> requestSpecification.auth().oauth2(value.tokenManager().getAccessTokenString()));

    return requestSpecification;
  }

  private RequestSpecification getRootRequest(Optional<Keycloak> optionalKeycloak) {
    RequestSpecification requestSpecification =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("/realms/" + REALM + "/")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString());

    optionalKeycloak.ifPresent(
        value -> requestSpecification.auth().oauth2(value.tokenManager().getAccessTokenString()));

    return requestSpecification;
  }

  protected void validateOrgInvitations(
      List<InvitationRepresentation> importInvitations,
      OrganizationRepresentation organizationRepresentation,
      String realm) {
    try {
      List<Invitation> invitations = getOrgInvitations(organizationRepresentation, realm);

      // validate invitations and parameters
      assertThat(importInvitations, hasSize(invitations.size()));
      invitations.forEach(
          invitation -> {
            var importedInvitation =
                importInvitations.stream()
                    .filter(invitationRep -> invitation.getEmail().equals(invitationRep.getEmail()))
                    .findFirst()
                    .orElseThrow();
            validateInvitationParameters(importedInvitation, invitation, realm);
          });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<Invitation> getOrgInvitations(
      OrganizationRepresentation organizationRepresentation, String realm)
      throws JsonProcessingException {

    var invitationsResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath(
                "realms/" + realm + "/orgs/" + organizationRepresentation.getId() + "/invitations")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();
    return objectMapper()
        .readValue(invitationsResponse.getBody().asString(), new TypeReference<>() {});
  }

  private void validateInvitationParameters(
      InvitationRepresentation invitationRepresentation, Invitation invitation, String realm) {
    var inviter =
        keycloak
            .realm(realm)
            .users()
            .searchByUsername(invitationRepresentation.getInviterUsername(), true)
            .stream()
            .findFirst()
            .orElseThrow();
    assertThat(inviter.getId(), Matchers.is(invitation.getInviterId()));
    assertThat(
        invitationRepresentation.getRedirectUri(), Matchers.is(invitation.getInvitationUrl()));
    assertThat(invitationRepresentation.getEmail(), Matchers.is(invitation.getEmail()));

    if (invitationRepresentation.getRoles().isEmpty()) {
      assertThat(invitationRepresentation.getRoles(), hasSize(invitation.getRoles().size()));
    } else {
      assertThat(
          invitationRepresentation.getRoles(), containsInAnyOrder(invitation.getRoles().toArray()));
    }

    if (invitationRepresentation.getAttributes().isEmpty()) {
      assertThat(invitationRepresentation.getAttributes().entrySet(), hasSize(0));
    } else {
      assertThat(
          invitationRepresentation.getAttributes().entrySet(),
          containsInAnyOrder(invitation.getAttributes().entrySet().toArray()));
    }
  }

  protected void validateOrgMembers(
      List<UserRolesRepresentation> exportedMembers,
      OrganizationRepresentation organizationRepresentation,
      String realm) {
    try {
      var managedUsers = getOrgMembers(organizationRepresentation, realm);

      assertThat(managedUsers, hasSize(exportedMembers.size()));

      List<String> expectedImportedById = exportedMembers.stream()
          .map(UserRolesRepresentation::getId)
          .filter(id -> id != null && !id.isEmpty())
          .toList();

      List<String> expectedImportedByUsername = exportedMembers.stream()
          .map(UserRolesRepresentation::getUsername)
          .filter(username -> username != null && !username.isEmpty())
          .toList();

      List<String> importedById = managedUsers.stream()
          .map(UserRepresentation::getId)
          .toList();

      List<String> importedByUsername = managedUsers.stream()
          .map(UserRepresentation::getUsername)
          .toList();

      assertThat(importedById, hasItems(expectedImportedById.toArray(String[]::new)));
      assertThat(importedByUsername, hasItems(expectedImportedByUsername.toArray(String[]::new)));

      // validate roles
      managedUsers.forEach(
          userRepresentation -> {
            var member =
                exportedMembers.stream()
                    .filter(
                        importedMember -> {
                            // Either match against ID or username
                            String importedId = importedMember.getId();
                            String importedUsername = importedMember.getUsername();
                            String userId = userRepresentation.getId();
                            String userUsername = userRepresentation.getUsername();

                            if (importedId != null && !importedId.isEmpty()) {
                                return importedId.equals(userId);
                            } else if (importedUsername != null && !importedUsername.isEmpty()) {
                                return importedUsername.equals(userUsername);
                            }

                            return false;
                        })
                    .findFirst()
                    .orElseThrow();
            validateOrgRoles(userRepresentation, member, organizationRepresentation.getId(), realm);
          });

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  protected void validateOrgRoles(
      UserRepresentation userRepresentation,
      UserRolesRepresentation member,
      String orgId,
      String realm) {

    var rolesResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath(
                "realms/"
                    + realm
                    + "/users/"
                    + userRepresentation.getId()
                    + "/orgs/"
                    + orgId
                    + "/roles")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();
    try {
      List<OrganizationRole> roles =
          objectMapper().readValue(rolesResponse.getBody().asString(), new TypeReference<>() {});

      assertThat(roles, hasSize(member.getRoles().size()));
      assertThat(
          roles.stream().map(OrganizationRole::getName).toList(),
          containsInAnyOrder(member.getRoles().toArray()));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<UserRepresentation> getOrgMembers(
      OrganizationRepresentation organizationRepresentation, String realm)
      throws JsonProcessingException {

    var membersResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath(
                "realms/" + realm + "/orgs/" + organizationRepresentation.getId() + "/members")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();

    List<UserRepresentation> members =
        objectMapper().readValue(membersResponse.getBody().asString(), new TypeReference<>() {});

    return members.stream()
        .filter(
            member ->
                !String.format("org-admin-%s", organizationRepresentation.getId())
                    .equals(member.getUsername()))
        .toList();
  }

  protected void validateOrg(
      io.phasetwo.service.importexport.representation.OrganizationRepresentation
          exportOrganizationReprezentation,
      OrganizationRepresentation rep) {
    assertThat(
        exportOrganizationReprezentation.getOrganization().getName(), Matchers.is(rep.getName()));

    if (rep.getDomains().isEmpty()) {
      assertThat(exportOrganizationReprezentation.getOrganization().getDomains(), hasSize(0));
    } else {
      assertThat(
          exportOrganizationReprezentation.getOrganization().getDomains(),
          contains(rep.getDomains().toArray()));
    }
    assertThat(
        exportOrganizationReprezentation.getOrganization().getUrl(), Matchers.is(rep.getUrl()));
    assertThat(
        exportOrganizationReprezentation.getOrganization().getDisplayName(),
        Matchers.is(rep.getDisplayName()));
    if (rep.getAttributes().isEmpty()) {
      assertThat(
          exportOrganizationReprezentation.getOrganization().getAttributes().entrySet(),
          hasSize(0));
    } else {
      assertThat(
          exportOrganizationReprezentation.getOrganization().getAttributes().entrySet(),
          contains(rep.getAttributes().entrySet().toArray()));
    }
  }

  protected ClientRepresentation createPublicClientInRealm(RealmResource realm, String clientId) {
    ClientRepresentation client = new ClientRepresentation();
    client.setClientId(clientId);
    client.setName(clientId);
    client.setPublicClient(true);
    client.setServiceAccountsEnabled(false);
    client.setDirectAccessGrantsEnabled(true);
    client.setEnabled(true);
    client.setSecret("secret");
    client.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
    client.setFullScopeAllowed(false);
    realm.clients().create(client).close();

    return realm.clients().findByClientId(client.getClientId()).getFirst();
  }
}
