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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class OrganizationMembersImportTest extends AbstractOrganizationTest {
  private final String realm = "org-realm";

  @Test
  void testOrganizationMembersImport() throws IOException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream("/orgs/membership-test/org-members-import-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass().getResourceAsStream("/orgs/membership-test/org-members-import-test.json"),
            KeycloakOrgsRepresentation.class);

    // import orgs
    var importRealmResponse = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(importRealmResponse.getStatusCode(), is(Response.Status.OK.getStatusCode()));

    // validate
    List<OrganizationRepresentation> organizations =
        new KeycloakOrgsAdminAPI(container.getAuthServerUrl(), realm, keycloak).listOrganizations();
    assertThat(organizations, hasSize(2));

    // test org1
    var org1 =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test1"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test1"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrgMembers(org1.getMembers(), organizationRepresentation, realm);
            });

    // test org2
    var org2 =
        orgsRepresentation.getOrganizations().stream()
            .filter(organization -> organization.getOrganization().getName().equals("test2"))
            .findFirst()
            .orElseThrow();

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test2"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              validateOrgMembers(org2.getMembers(), organizationRepresentation, realm);
            });

    // users
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(2));
  }

  @Test
  void testOrganizationMemberImportMissingUser() {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/membership-test/org-member-import-missing-user-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation keycloakOrgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/membership-test/org-member-import-missing-user-test.json"),
            KeycloakOrgsRepresentation.class);

    // import realm
    var response = importOrgs(keycloakOrgsRepresentation, keycloak, realm);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationMemberImportMissingUserWithSkipMissingMembers()
      throws JsonProcessingException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/membership-test/org-member-import-missing-user-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation keycloakOrgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/membership-test/org-member-import-missing-user-test.json"),
            KeycloakOrgsRepresentation.class);

    // import realm
    var orgResponse = importOrgsSkipMissingMembers(keycloakOrgsRepresentation, keycloak, realm);
    assertThat(orgResponse.getStatusCode(), is(Response.Status.OK.getStatusCode()));

    // validate
    List<OrganizationRepresentation> organizations =
        new KeycloakOrgsAdminAPI(container.getAuthServerUrl(), realm, keycloak).listOrganizations();
    assertThat(organizations, hasSize(1));

    // test org1

    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test1"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              try {
                List<UserRepresentation> orgMembers =
                    getOrgMembers(organizationRepresentation, realm);
                assertThat(orgMembers, hasSize(1));
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            });
    // users
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(1));
  }

  @AfterEach
  public void afterEach() {
    // delete realm
    keycloak.realm(realm).remove();
  }
}
