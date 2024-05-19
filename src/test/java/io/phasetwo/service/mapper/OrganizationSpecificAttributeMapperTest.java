package io.phasetwo.service.mapper;

import static io.phasetwo.service.Helpers.createUserWithCredentials;
import static io.phasetwo.service.Helpers.deleteUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.phasetwo.client.openapi.model.OrganizationRepresentation;
import io.phasetwo.service.AbstractOrganizationTest;
import io.phasetwo.service.protocol.oidc.mappers.OrganizationSpecificAttributeMapper;
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

@JBossLog
class OrganizationSpecificAttributeMapperTest extends AbstractOrganizationTest {

  public static final String CLAIM = "secret_attr";

  @Test
  void shouldConfigureOrganizationSpecificAttributeMapperOidcProtocolMapper() throws Exception {
    // add organization with attribute 'secret_attr'
    var organization = createOrganization(
        new OrganizationRepresentation()
            .name("example1")
            .domains(List.of("example1.com"))
            .url("www.example1.com")
            .displayName("Example")
            .attributes(Map.of(CLAIM, List.of("My Secret Value"))));
    String id = organization.getId();

    // add the user to organization
    final UserRepresentation user = createUserWithCredentials(keycloak, REALM, "jdoe", "pass");
    Response response = putRequest("foo", id, "members", user.getId());
    assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));

    RealmResource realm = keycloak.realm(REALM);
    ClientRepresentation client = realm.clients().findByClientId(ADMIN_CLI).get(0);

    // parse the received access-token
    configureCustomOidcProtocolMapper(realm, client);

    keycloak = getKeycloak(REALM, ADMIN_CLI, user.getUsername(), "pass");

    TokenVerifier<AccessToken> verifier = TokenVerifier.create(keycloak.tokenManager().getAccessTokenString(),
        AccessToken.class);
    verifier.parse();

    // check for the custom claim
    AccessToken accessToken = verifier.getToken();
    Map<String, Object> customClaimValue = (Map<String, Object>) accessToken.getOtherClaims().get(CLAIM);
    log.debugf("Custom Claim name secret_attr= %s", customClaimValue.toString());

    // check the attribute values
    assertNotNull(customClaimValue);
    assertThat(customClaimValue.containsKey(id), is(true));
    assertEquals("My Secret Value", customClaimValue.get(id));

    // change authorization
    keycloak = getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
    // delete user
    deleteUser(keycloak, REALM, user.getId());
    // delete organization
    deleteOrganization(keycloak, id);
  }

  private static void configureCustomOidcProtocolMapper(RealmResource realm, ClientRepresentation client) {

    ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
    mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
    mapper.setProtocolMapper(OrganizationSpecificAttributeMapper.PROVIDER_ID);
    // Claim name is same as attribute name
    mapper.setName(CLAIM);

    Map<String, String> config = new HashMap<>();
    config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, CLAIM);
    config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
    mapper.setConfig(config);

    realm.clients().get(client.getId()).getProtocolMappers().createMapper(mapper).close();
  }
}
