package io.phasetwo.service.globalconfig;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.OrganizationsConfig;
import io.restassured.response.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class CreateAdminUserEnabledTest extends AbstractOrganizationTest {

  @Test
  void testOrganizationCreation() throws IOException {
    // create organization
    var organization1 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example-org")
                .domains(List.of("example.com", "test.org")));
    // create organization 2
    var organization2 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example-org2")
                .domains(List.of("example2.com", "test2.org")));

    Response response1 = getRequest(organization1.getId(), "members");
    assertThat(response1.statusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));

    // get empty members list
    List<UserRepresentation> org1Members =
        objectMapper().readValue(response1.getBody().asString(), new TypeReference<>() {});
    assertThat(org1Members, notNullValue());
    assertThat(org1Members, hasSize(0)); // org admin default doesn't exist

    Response response2 = getRequest(organization2.getId(), "members");
    assertThat(response2.statusCode(), is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));

    // get empty members list
    List<UserRepresentation> membersOrg2 =
        objectMapper().readValue(response2.getBody().asString(), new TypeReference<>() {});
    assertThat(membersOrg2, notNullValue());
    assertThat(org1Members, hasSize(0)); // org admin default doesn't exist

    // delete org1
    deleteOrganization(organization1.getId());

    // delete org2
    deleteOrganization(organization2.getId());
  }

  @BeforeEach
  public void beforeEach() throws JsonProcessingException {
    // remove create admin user idp master config
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setCreateAdminUser(false);
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
    orgConfig.setCreateAdminUser(true);

    var responseOrgsConfig = putRequest(orgConfig, url);
    assertThat(
        responseOrgsConfig.getStatusCode(),
        is(jakarta.ws.rs.core.Response.Status.OK.getStatusCode()));
  }
}
