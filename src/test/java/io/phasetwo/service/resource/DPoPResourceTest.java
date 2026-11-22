package io.phasetwo.service.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.phasetwo.service.AbstractOrganizationTest;
import io.restassured.response.Response;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Calls the org admin endpoints the way a browser client with DPoP enabled does: a DPoP-bound
 * access token sent as {@code Authorization: DPoP <token>} plus a fresh {@code DPoP} proof per
 * request (RFC 9449).
 */
class DPoPResourceTest extends AbstractOrganizationTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

  private static KeyPair keyPair;

  @BeforeAll
  static void generateKey() throws Exception {
    KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
    gen.initialize(2048);
    keyPair = gen.generateKeyPair();
  }

  @Test
  void dpopBoundTokenWithFreshProofIsAccepted() throws Exception {
    String token = dpopBoundToken();
    String url = orgsUrl();

    Response first = dpopGet(url, token, proof("GET", url, token));
    assertThat(first.getStatusCode(), is(200));

    // each request carries its own proof, as a browser client does
    Response second = dpopGet(url, token, proof("GET", url, token));
    assertThat(second.getStatusCode(), is(200));
  }

  @Test
  void replayedProofIsRejected() throws Exception {
    String token = dpopBoundToken();
    String url = orgsUrl();
    String proof = proof("GET", url, token);

    assertThat(dpopGet(url, token, proof).getStatusCode(), is(200));
    assertThat(dpopGet(url, token, proof).getStatusCode(), is(401));
  }

  @Test
  void dpopBoundTokenWithoutProofIsRejected() throws Exception {
    String token = dpopBoundToken();
    String url = orgsUrl();

    Response noProof = given().header("Authorization", "DPoP " + token).when().get(url);
    assertThat(noProof.getStatusCode(), is(401));

    Response asBearer = given().header("Authorization", "Bearer " + token).when().get(url);
    assertThat(asBearer.getStatusCode(), is(401));
  }

  @Test
  void proofForAnotherUrlIsRejected() throws Exception {
    String token = dpopBoundToken();
    String url = orgsUrl();

    Response wrongUrl = dpopGet(url, token, proof("GET", url + "/count", token));
    assertThat(wrongUrl.getStatusCode(), is(401));

    Response wrongMethod = dpopGet(url, token, proof("POST", url, token));
    assertThat(wrongMethod.getStatusCode(), is(401));
  }

  @Test
  void preflightAllowsDPoPHeader() {
    Response preflight =
        given()
            .header("Origin", "https://app.example.com")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization,dpop")
            .when()
            .options(orgsUrl());
    assertThat(preflight.getStatusCode(), is(200));
    assertThat(
        preflight.getHeader("Access-Control-Allow-Headers").toLowerCase(), containsString("dpop"));
  }

  private static String orgsUrl() {
    return getAuthUrl() + "/realms/" + REALM + "/orgs";
  }

  private static Response dpopGet(String url, String token, String proof) {
    return given().header("Authorization", "DPoP " + token).header("DPoP", proof).when().get(url);
  }

  private String dpopBoundToken() throws Exception {
    String tokenUrl = getAuthUrl() + "/realms/" + REALM + "/protocol/openid-connect/token";
    Response response =
        given()
            .header("DPoP", proof("POST", tokenUrl, null))
            .formParam("grant_type", "password")
            .formParam("client_id", ADMIN_CLI)
            .formParam("username", container.getAdminUsername())
            .formParam("password", container.getAdminPassword())
            .when()
            .post(tokenUrl);
    assertThat(response.getStatusCode(), is(200));
    JsonNode body = MAPPER.readTree(response.asString());
    assertThat(body.get("token_type").asText(), equalToIgnoringCase("DPoP"));
    String accessToken = body.get("access_token").asText();
    JsonNode claims = MAPPER.readTree(Base64.getUrlDecoder().decode(accessToken.split("\\.")[1]));
    assertThat(claims.path("cnf").path("jkt").asText(null), notNullValue());
    return accessToken;
  }

  private static String proof(String method, String url, String accessToken) throws Exception {
    RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
    Map<String, Object> jwk = new LinkedHashMap<>();
    jwk.put("kty", "RSA");
    jwk.put("n", B64.encodeToString(unsigned(pub.getModulus().toByteArray())));
    jwk.put("e", B64.encodeToString(unsigned(pub.getPublicExponent().toByteArray())));

    Map<String, Object> header = new LinkedHashMap<>();
    header.put("typ", "dpop+jwt");
    header.put("alg", "RS256");
    header.put("jwk", jwk);

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("jti", UUID.randomUUID().toString());
    payload.put("htm", method);
    payload.put("htu", url);
    payload.put("iat", System.currentTimeMillis() / 1000);
    if (accessToken != null) {
      byte[] hash =
          MessageDigest.getInstance("SHA-256")
              .digest(accessToken.getBytes(StandardCharsets.US_ASCII));
      payload.put("ath", B64.encodeToString(hash));
    }

    String signingInput =
        B64.encodeToString(MAPPER.writeValueAsBytes(header))
            + "."
            + B64.encodeToString(MAPPER.writeValueAsBytes(payload));
    Signature signer = Signature.getInstance("SHA256withRSA");
    signer.initSign(keyPair.getPrivate());
    signer.update(signingInput.getBytes(StandardCharsets.US_ASCII));
    return signingInput + "." + B64.encodeToString(signer.sign());
  }

  private static byte[] unsigned(byte[] bytes) {
    if (bytes.length > 1 && bytes[0] == 0) {
      byte[] trimmed = new byte[bytes.length - 1];
      System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
      return trimmed;
    }
    return bytes;
  }
}
