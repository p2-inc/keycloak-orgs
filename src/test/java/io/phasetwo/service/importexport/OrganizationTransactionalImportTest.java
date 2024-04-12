package io.phasetwo.service.importexport;

import static io.phasetwo.service.Helpers.loadJson;
import static io.phasetwo.service.Helpers.objectMapper;
import static io.phasetwo.service.Helpers.toJsonString;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;

@JBossLog
public class OrganizationTransactionalImportTest extends AbstractOrganizationTest {
  private final String realm = "org-realm";

  @Test
  void testOrganizationsTransactionalImport() throws JsonProcessingException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass().getResourceAsStream("/orgs/keycloak-realm-with-identity-provider.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // create second organization which exists in the import json
    var organizationRepresentation = new OrganizationRepresentation().name("test3");
    var createOrgResponse =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .body(toJsonString(organizationRepresentation))
            .when()
            .post()
            .andReturn();

    assertThat(createOrgResponse.getStatusCode(), is(Response.Status.CREATED.getStatusCode()));

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-import-test.json"),
            KeycloakOrgsRepresentation.class);
    var orgsResponse = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(
        orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.CONFLICT.getStatusCode()));

    // validate
    // get organizations
    var response =
        given()
            .baseUri(container.getAuthServerUrl())
            .basePath("realms/" + realm + "/orgs")
            .contentType("application/json")
            .auth()
            .oauth2(keycloak.tokenManager().getAccessTokenString())
            .and()
            .when()
            .get()
            .then()
            .extract()
            .response();

    List<OrganizationRepresentation> organizations =
        objectMapper().readValue(response.getBody().asString(), new TypeReference<>() {});
    assertThat(organizations, hasSize(1));
  }

  @AfterEach
  public void afterEach() {
    // delete realm
    keycloak.realm(realm).remove();
  }
}
