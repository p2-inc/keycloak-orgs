package io.phasetwo.service.resource;

import static io.phasetwo.service.Helpers.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.representation.*;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

@JBossLog
class ScimProviderResourceTest extends AbstractOrganizationTest {

  @org.junit.jupiter.api.BeforeEach
  public void enableScimFeature() throws JsonProcessingException {
    setScimRealmFeature(true);
  }

  @AfterEach
  public void afterEach() throws JsonProcessingException {
    setScimRealmFeature(false);
    final var orgs = listOrganizations();
    for (final var org : orgs) {
      deleteOrganization(org.getId());
    }
  }

  private void setScimRealmFeature(boolean enabled) throws JsonProcessingException {
    var url = getAuthUrl() + "/realms/master/orgs/config";
    var orgConfig = new OrganizationsConfig();
    orgConfig.setScimEnabled(enabled);
    var resp = putRequest(orgConfig, url);
    assertThat(resp.getStatusCode(), is(Status.OK.getStatusCode()));
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

  @Test
  void testCleartextSharedSecretIsHashedOnCreate() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    SharedSecretScimAuth secretAuth = new SharedSecretScimAuth();
    secretAuth.setSharedSecret("super-secret-cleartext");
    rep.setAuth(secretAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);

    SharedSecretScimAuth resultAuth = (SharedSecretScimAuth) result.getAuth();
    String stored = resultAuth.getSharedSecret();
    assertThat("server must not return the cleartext", stored, not(is("super-secret-cleartext")));
    assertThat("stored value must be a PHC argon2id hash", stored, startsWith("$argon2id$"));
  }

  @Test
  void testAlreadyHashedSharedSecretIsPreservedOnCreate() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    String existingHash =
        "$argon2id$v=19$m=16,t=2,p=1$Z21uSVZmSFBxbzcycnZpdA$SJtF8lsYQ5vSysKtGBKIdg";

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    SharedSecretScimAuth secretAuth = new SharedSecretScimAuth();
    secretAuth.setSharedSecret(existingHash);
    rep.setAuth(secretAuth);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    SharedSecretScimAuth resultAuth = (SharedSecretScimAuth) result.getAuth();
    assertThat(
        "argon-prefixed input must not be re-hashed",
        resultAuth.getSharedSecret(),
        is(existingHash));
  }

  @Test
  void testCleartextBasicPasswordIsHashedOnCreate() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    BasicAuthScimAuth basic = new BasicAuthScimAuth();
    basic.setUsername("scim-admin");
    basic.setPassword("plaintext-pass");
    rep.setAuth(basic);

    Response response = postRequest(rep, orgId, "scim");
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    response = getRequest(orgId, "scim");
    OrganizationScimRepresentation result =
        objectMapper()
            .readValue(response.getBody().asString(), OrganizationScimRepresentation.class);
    BasicAuthScimAuth resultAuth = (BasicAuthScimAuth) result.getAuth();
    assertThat(resultAuth.getUsername(), is("scim-admin"));
    assertThat("username is never hashed", resultAuth.getUsername(), not(startsWith("$argon2")));
    assertThat(
        "password is hashed before storage", resultAuth.getPassword(), startsWith("$argon2id$"));
    assertThat(resultAuth.getPassword(), not(is("plaintext-pass")));
  }

  @Test
  void testPutWithSameHashedSecretIsIdempotent() throws IOException {
    // Models the Admin UI's "leave password blank → keep existing hash" flow:
    // the UI re-sends the existing hash, which the encoder must treat as
    // already-hashed and leave alone.
    var org = createDefaultOrg();
    String orgId = org.getId();

    // First, create with a cleartext secret and capture the hash the server stored.
    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    SharedSecretScimAuth secretAuth = new SharedSecretScimAuth();
    secretAuth.setSharedSecret("first-cleartext");
    rep.setAuth(secretAuth);
    assertThat(postRequest(rep, orgId, "scim").getStatusCode(), is(Status.CREATED.getStatusCode()));

    Response getResp = getRequest(orgId, "scim");
    String storedHash =
        ((SharedSecretScimAuth)
                objectMapper()
                    .readValue(getResp.getBody().asString(), OrganizationScimRepresentation.class)
                    .getAuth())
            .getSharedSecret();
    assertThat(storedHash, startsWith("$argon2id$"));

    // PUT with the existing hash echoed back (simulating the UI flow).
    OrganizationScimRepresentation updateRep = new OrganizationScimRepresentation();
    updateRep.setEnabled(true);
    SharedSecretScimAuth echoed = new SharedSecretScimAuth();
    echoed.setSharedSecret(storedHash);
    updateRep.setAuth(echoed);

    Response putResp = putRequest(updateRep, orgId, "scim");
    assertThat(putResp.getStatusCode(), is(Status.OK.getStatusCode()));

    OrganizationScimRepresentation after =
        objectMapper()
            .readValue(
                getRequest(orgId, "scim").getBody().asString(),
                OrganizationScimRepresentation.class);
    assertThat(
        "hash should round-trip unchanged",
        ((SharedSecretScimAuth) after.getAuth()).getSharedSecret(),
        is(storedHash));
  }

  @Test
  void testPutWithNewCleartextSecretReplacesHash() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    SharedSecretScimAuth secretAuth = new SharedSecretScimAuth();
    secretAuth.setSharedSecret("first-cleartext");
    rep.setAuth(secretAuth);
    postRequest(rep, orgId, "scim");

    String firstHash =
        ((SharedSecretScimAuth)
                objectMapper()
                    .readValue(
                        getRequest(orgId, "scim").getBody().asString(),
                        OrganizationScimRepresentation.class)
                    .getAuth())
            .getSharedSecret();

    OrganizationScimRepresentation updateRep = new OrganizationScimRepresentation();
    updateRep.setEnabled(true);
    SharedSecretScimAuth replaced = new SharedSecretScimAuth();
    replaced.setSharedSecret("second-cleartext");
    updateRep.setAuth(replaced);
    putRequest(updateRep, orgId, "scim");

    String secondHash =
        ((SharedSecretScimAuth)
                objectMapper()
                    .readValue(
                        getRequest(orgId, "scim").getBody().asString(),
                        OrganizationScimRepresentation.class)
                    .getAuth())
            .getSharedSecret();

    assertThat(secondHash, startsWith("$argon2id$"));
    assertThat(
        "different cleartext should produce a different hash", secondHash, not(is(firstHash)));
  }

  @Test
  void testScimDisabledReturns404() throws IOException {
    var org = createDefaultOrg();
    String orgId = org.getId();

    // Flip the realm-level feature off (overrides BeforeEach for this test).
    setScimRealmFeature(false);

    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(true);
    rep.setAuth(new KeycloakScimAuth());

    // GET / POST / PUT / DELETE all return 404 when the feature is off.
    assertThat(getRequest(orgId, "scim").getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
    assertThat(
        postRequest(rep, orgId, "scim").getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
    assertThat(
        putRequest(rep, orgId, "scim").getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
    assertThat(deleteRequest(orgId, "scim").getStatusCode(), is(Status.NOT_FOUND.getStatusCode()));
  }
}
