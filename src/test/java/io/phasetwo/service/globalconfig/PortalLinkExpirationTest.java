package io.phasetwo.service.globalconfig;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.client.openapi.model.PortalLinkRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.OrganizationsConfig;
import io.restassured.http.Header;
import io.restassured.response.Response;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.common.util.Base64Url;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.util.JsonSerialization;

@JBossLog
public class PortalLinkExpirationTest extends AbstractOrganizationTest {

  @Test
  void testOrganizationPortalLinkExpiration() throws IOException, URISyntaxException {
    OrganizationRepresentation org = createDefaultOrg();
    String id = org.getId();

    // create wizard client
    ClientRepresentation clientRepresentation = new ClientRepresentation();
    clientRepresentation.setName("idp-wizard");
    clientRepresentation.setId("idp-wizard");
    keycloak.realm(REALM).clients().create(clientRepresentation);
    Response response =
        givenSpec()
            .header(new Header("content-type", MediaType.APPLICATION_FORM_URLENCODED))
            .formParam("baseUri", "foobar")
            .when()
            .post("/%s/portal-link".formatted(id))
            .then()
            .extract()
            .response();

    assertThat(response.getStatusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));

    UserRepresentation orgAdmin =
        keycloak.realm(REALM).users().search("org-admin-%s".formatted(id)).get(0);

    PortalLinkRepresentation link =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});

    assertThat(link, notNullValue());
    assertThat(link.getLink(), notNullValue());

    Optional<String> keyValue = getQueryParamValue(link.getLink(), "key");
    assertTrue(keyValue.isPresent());
    var body = parseBody(keyValue.get());
    var iat = body.get("iat").asLong();
    var exp = body.get("exp").asLong();

    long lifespan = exp - iat;
    assert lifespan == 300;

    // delete org
    deleteOrganization(id);
    // delete client
    keycloak.realm(REALM).clients().get("idp-wizard").remove();
  }

  public static Optional<String> getQueryParamValue(String urlString, String paramName)
      throws URISyntaxException {
    URI uri = new URI(urlString);
    String query = uri.getQuery();

    if (query == null || query.isEmpty()) {
      return Optional.empty();
    }

    return Arrays.stream(query.split("&"))
        .map(param -> param.split("=", 2))
        .filter(pair -> pair.length == 2 && pair[0].equals(paramName))
        .map(pair -> pair[1])
        .findFirst();
  }

  @BeforeEach
  public void beforeEach() throws JsonProcessingException {
    // remove create admin user idp master config
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setExpirationInSecs(300);
    var responseOrgsConfig = putRequest(orgConfig, url);
    assertThat(
        responseOrgsConfig.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
  }

  @AfterEach
  public void afterEach() throws JsonProcessingException {
    // add create admin user shared idp master config
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setExpirationInSecs(86400);

    var responseOrgsConfig = putRequest(orgConfig, url);
    assertThat(
        responseOrgsConfig.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
  }

  public static JsonNode parseBody(String jwt) {
    String[] parts = jwt.split("\\.");

    if (parts.length == 0) {
      throw new RuntimeException("Could not infer header from JWT");
    }

    JsonNode header;

    try {
      return JsonSerialization.readValue(Base64Url.decode(parts[1]), JsonNode.class);
    } catch (IOException cause) {
      throw new RuntimeException("Failed to parse JWT header", cause);
    }
  }
}
