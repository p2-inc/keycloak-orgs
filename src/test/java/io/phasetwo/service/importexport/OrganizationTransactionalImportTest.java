package io.phasetwo.service.importexport;

import static io.phasetwo.service.Helpers.loadJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.KeycloakOrgsAdminAPI;
import io.phasetwo.service.importexport.representation.KeycloakOrgsRepresentation;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
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
    new KeycloakOrgsAdminAPI(container.getAuthServerUrl(), realm, keycloak)
        .createOrganization(new OrganizationRepresentation().name("test3"));

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/org-import-test.json"),
            KeycloakOrgsRepresentation.class);
    var orgsResponse = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(
        orgsResponse.getStatusCode(), CoreMatchers.is(Response.Status.CONFLICT.getStatusCode()));

    // validate
    List<OrganizationRepresentation> organizations =
        new KeycloakOrgsAdminAPI(container.getAuthServerUrl(), realm, keycloak).listOrganizations();
    assertThat(organizations, hasSize(1));
  }

  @AfterEach
  public void afterEach() {
    // delete realm
    keycloak.realm(realm).remove();
  }
}
