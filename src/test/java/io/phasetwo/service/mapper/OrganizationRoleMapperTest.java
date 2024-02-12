package io.phasetwo.service.mapper;

import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.protocol.oidc.mappers.OrganizationRoleMapper;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.junit.jupiter.api.Test;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.junit.jupiter.Testcontainers;

@JBossLog
@Testcontainers
class OrganizationRoleMapperTest extends AbstractOrganizationTest {

  public static final String CLAIM = "organizations";

  @Test
  void shouldConfigureOrganizationRoleOidcProtocolMapper() throws Exception {
    // add org
    String id = createDefaultOrg().getId();

    final UserRepresentation user = createUserWithCredentials(keycloak, REALM, "jdoe", "pass");

    Response response = putRequest("foo", id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    List<String> roles =
        List.of(
            OrganizationAdminAuth.ORG_ROLE_VIEW_ORGANIZATION,
            OrganizationAdminAuth.ORG_ROLE_MANAGE_ORGANIZATION);

    for (String roleName : roles) {
      grantUserRole(id, roleName, user.getId());
    }

    RealmResource realm = keycloak.realm(REALM);
    ClientRepresentation client = realm.clients().findByClientId(ADMIN_CLI).get(0);

    // parse the received access-token
    configureCustomOidcProtocolMapper(realm, client);

    keycloak = getKeycloak(REALM, ADMIN_CLI, user.getUsername(), "pass");

    TokenVerifier<AccessToken> verifier =
        TokenVerifier.create(keycloak.tokenManager().getAccessTokenString(), AccessToken.class);
    verifier.parse();

    // check for the custom claim
    AccessToken accessToken = verifier.getToken();

    Map<String, Object> customClaimValue =
        (Map<String, Object>) accessToken.getOtherClaims().get(CLAIM);
    log.debugf("Custom Claim name organizations= %s", customClaimValue.toString());
    assertNotNull(customClaimValue);
    assertThat(customClaimValue.containsKey(id), is(true));
    assertEquals(
        new HashMap<String, Object>() {
          {
            put("name", "example");
            put("roles", roles);
          }
        },
        customClaimValue.get(id));
  }

  private static void configureCustomOidcProtocolMapper(
      RealmResource realm, ClientRepresentation client) {
    ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
    mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
    mapper.setProtocolMapper(OrganizationRoleMapper.PROVIDER_ID);
    mapper.setName("test-oidc-company-role-mapper");

    Map<String, String> config = new HashMap<>();
    config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, CLAIM);
    config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
    mapper.setConfig(config);

    realm.clients().get(client.getId()).getProtocolMappers().createMapper(mapper).close();
  }
}
