package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.*;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@JBossLog
class ScimProviderResourceTest extends AbstractOrganizationTest {

  @AfterEach
  public void afterEach() throws JsonProcessingException {
    final var orgs = listOrganizations();
    for (final var org : orgs) {
      deleteOrganization(org.getId());
    }
  }

  @Test
  void testCreateAndGetScimConfig() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    // Create JWT auth config
    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    rep.setEmailAsUsername(false);
    rep.setLinkIdp(true);
    JwtScimAuth jwtAuth = new JwtScimAuth();
    jwtAuth.setIssuer("https://issuer.example.com");
    jwtAuth.setAudience("my-audience");
    jwtAuth.setJwksUri("https://issuer.example.com/.well-known/jwks.json");
    rep.setAuth(jwtAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // GET it back
    response = getRequest(orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    assertThat(result.getEnabled(), is(true));
    assertThat(result.getEmailAsUsername(), is(false));
    assertThat(result.getLinkIdp(), is(true));
    assertThat(result.getAuth(), is(notNullValue()));
    assertThat(result.getAuth() instanceof JwtScimAuth, is(true));

    JwtScimAuth resultAuth = (JwtScimAuth) result.getAuth();
    assertThat(resultAuth.getIssuer(), is("https://issuer.example.com"));
    assertThat(resultAuth.getAudience(), is("my-audience"));
    assertThat(resultAuth.getJwksUri(), is("https://issuer.example.com/.well-known/jwks.json"));
  }

  @Test
  void testUpdateScimConfig() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    // Create with JWT
    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    JwtScimAuth jwtAuth = new JwtScimAuth();
    jwtAuth.setIssuer("https://issuer.example.com");
    jwtAuth.setAudience("aud");
    jwtAuth.setJwksUri("https://issuer.example.com/jwks");
    rep.setAuth(jwtAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // Update to BasicAuth
    OrganizationScimRepresentation updateRep = new OrganizationScimRepresentation();
    updateRep.setEnabled(true);
    updateRep.setEmailAsUsername(true);
    BasicAuthScimAuth basicAuth = new BasicAuthScimAuth();
    basicAuth.setUsername("scim-admin");
    basicAuth.setPassword("$argon2id$v=19$m=16,t=2,p=1$hash");
    updateRep.setAuth(basicAuth);

    response = putRequest(updateRep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    // Verify update
    response = getRequest(orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.OK.getStatusCode()));

    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    assertThat(result.getEmailAsUsername(), is(true));
    assertThat(result.getAuth() instanceof BasicAuthScimAuth, is(true));

    BasicAuthScimAuth resultAuth = (BasicAuthScimAuth) result.getAuth();
    assertThat(resultAuth.getUsername(), is("scim-admin"));
    assertThat(resultAuth.getPassword(), is("$argon2id$v=19$m=16,t=2,p=1$hash"));
  }

  @Test
  void testDeleteScimConfig() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    // Create config
    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    rep.setAuth(new KeycloakScimAuth());

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // Delete
    response = deleteRequest(orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.NO_CONTENT.getStatusCode()));

    // Verify gone
    response = getRequest(orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  void testCreateDuplicate() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    rep.setAuth(new KeycloakScimAuth());

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    // Try to create again
    response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CONFLICT.getStatusCode()));
  }

  @Test
  void testGetNonExistent() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    Response response = getRequest(orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
  }

  @Test
  void testKeycloakAuthType() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    rep.setAuth(new KeycloakScimAuth());

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    assertThat(result.getAuth() instanceof KeycloakScimAuth, is(true));
    assertThat(result.getAuth().getType(), is("KEYCLOAK"));
  }

  @Test
  void testSharedSecretAuthType() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    SharedSecretScimAuth secretAuth = new SharedSecretScimAuth();
    secretAuth.setSharedSecret("$argon2id$v=19$m=16,t=2,p=1$secrethash");
    rep.setAuth(secretAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    assertThat(result.getAuth() instanceof SharedSecretScimAuth, is(true));
    assertThat(result.getAuth().getType(), is("EXTERNAL_SECRET"));

    SharedSecretScimAuth resultAuth = (SharedSecretScimAuth) result.getAuth();
    assertThat(resultAuth.getSharedSecret(), is("$argon2id$v=19$m=16,t=2,p=1$secrethash"));
  }

  @Test
  void testBasicAuthType() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    BasicAuthScimAuth basicAuth = new BasicAuthScimAuth();
    basicAuth.setUsername("admin");
    basicAuth.setPassword("$argon2id$v=19$m=16,t=2,p=1$passhash");
    rep.setAuth(basicAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    assertThat(result.getAuth() instanceof BasicAuthScimAuth, is(true));
    assertThat(result.getAuth().getType(), is("EXTERNAL_BASIC"));

    BasicAuthScimAuth resultAuth = (BasicAuthScimAuth) result.getAuth();
    assertThat(resultAuth.getUsername(), is("admin"));
    assertThat(resultAuth.getPassword(), is("$argon2id$v=19$m=16,t=2,p=1$passhash"));
  }

  @Test
  void testJwtAuthType() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    JwtScimAuth jwtAuth = new JwtScimAuth();
    jwtAuth.setIssuer("https://sts.windows.net/tenant-id/");
    jwtAuth.setAudience("8adf8e6e-67b2-4cf2-a259-e3dc5476c621");
    jwtAuth.setJwksUri("https://login.microsoftonline.com/tenant-id/discovery/v2.0/keys");
    rep.setAuth(jwtAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    assertThat(result.getAuth() instanceof JwtScimAuth, is(true));
    assertThat(result.getAuth().getType(), is("EXTERNAL_JWT"));

    JwtScimAuth resultAuth = (JwtScimAuth) result.getAuth();
    assertThat(resultAuth.getIssuer(), is("https://sts.windows.net/tenant-id/"));
    assertThat(resultAuth.getAudience(), is("8adf8e6e-67b2-4cf2-a259-e3dc5476c621"));
    assertThat(
        resultAuth.getJwksUri(),
        is("https://login.microsoftonline.com/tenant-id/discovery/v2.0/keys"));
  }
}
