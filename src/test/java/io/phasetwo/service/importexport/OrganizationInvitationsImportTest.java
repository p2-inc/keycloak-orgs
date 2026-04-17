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
import io.phasetwo.service.representation.Invitation;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;

@JBossLog
public class OrganizationInvitationsImportTest extends AbstractOrganizationTest {
  private final String realm = "org-realm";

  @Test
  void testOrganizationInvitationsImport() throws IOException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream("/orgs/invitations-test/org-invitations-import-test.json"),
            KeycloakOrgsRepresentation.class);

    // import orgs
    var orgsImportResponse = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(orgsImportResponse.getStatusCode(), is(Response.Status.OK.getStatusCode()));

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
              validateOrgInvitations(org1.getInvitations(), organizationRepresentation, realm);
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
              validateOrgInvitations(org2.getInvitations(), organizationRepresentation, realm);
            });

    // users
    assertThat(keycloak.realm(realm).users().search("org-"), hasSize(2));
  }

  @Test
  void testOrganizationInvitationsExistingMemberFailedImport() {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-existing-member-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-existing-member-test.json"),
            KeycloakOrgsRepresentation.class);

    //  import orgs
    var response = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationInvitationsInviterMissingFailedImport() {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-missing-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-missing-test.json"),
            KeycloakOrgsRepresentation.class);

    //  import orgs
    var response = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationInvitationsInviterMissingSkipMissingMembersImport()
      throws JsonProcessingException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-missing-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-missing-test.json"),
            KeycloakOrgsRepresentation.class);

    //  import orgs
    var orgImportResponse = importOrgsSkipMissingMembers(orgsRepresentation, keycloak, realm);
    assertThat(orgImportResponse.getStatusCode(), is(Response.Status.OK.getStatusCode()));

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
                List<Invitation> invitations = getOrgInvitations(organizationRepresentation, realm);
                assertThat(invitations, hasSize(0));
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Test
  void testOrganizationInvitationsInviterNotMemberOfOrgFailedImport() {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-not-in-org-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-not-in-org-test.json"),
            KeycloakOrgsRepresentation.class);

    // import orgs
    var response = importOrgs(orgsRepresentation, keycloak, realm);
    assertThat(response.getStatusCode(), is(Response.Status.BAD_REQUEST.getStatusCode()));
  }

  @Test
  void testOrganizationInvitationsInviterNotMemberOfOrgSkipMissingMembers()
      throws JsonProcessingException {
    // import realm
    RealmRepresentation testRealm =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-not-in-org-test-realm.json"),
            RealmRepresentation.class);
    importRealm(testRealm, keycloak);

    // prepare data
    KeycloakOrgsRepresentation orgsRepresentation =
        loadJson(
            getClass()
                .getResourceAsStream(
                    "/orgs/invitations-test/org-invitations-import-inviter-not-in-org-test.json"),
            KeycloakOrgsRepresentation.class);

    // import orgs
    var orgImportResponse = importOrgsSkipMissingMembers(orgsRepresentation, keycloak, realm);
    assertThat(orgImportResponse.getStatusCode(), is(Response.Status.OK.getStatusCode()));

    // validate
    List<OrganizationRepresentation> organizations =
        new KeycloakOrgsAdminAPI(container.getAuthServerUrl(), realm, keycloak).listOrganizations();
    assertThat(organizations, hasSize(2));

    // test org1
    organizations.stream()
        .filter(organizationRepresentation -> organizationRepresentation.getName().equals("test1"))
        .findFirst()
        .ifPresent(
            organizationRepresentation -> {
              try {
                List<Invitation> invitations = getOrgInvitations(organizationRepresentation, realm);
                assertThat(invitations, hasSize(0));
              } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @AfterEach
  public void afterEach() {
    // delete realm
    keycloak.realm(realm).remove();
  }
}
