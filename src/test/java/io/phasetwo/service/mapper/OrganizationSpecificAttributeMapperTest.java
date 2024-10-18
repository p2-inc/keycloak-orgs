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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
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
  public static final String SECOND_CLAIM = "another_attribute";

  //@Test
  void shouldConfigureOrganizationSpecificAttributeMapperOidcProtocolMapper() throws Exception {
    // add Example 1 with attribute 'secret_attr' with value "My Secret Value"
    var organization1 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example1")
                .domains(List.of("example1.com"))
                .url("www.example1.com")
                .displayName("Example 1")
                .attributes(Map.of(CLAIM, List.of("My Secret Value"))));
    String organizationId1 = organization1.getId();

    // add Example 2 with attribute 'secret_attr', value "My Second Secret Value"
    var organization2 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example2")
                .domains(List.of("example2.com"))
                .url("www.example2.com")
                .displayName("Example 2")
                .attributes(Map.of(CLAIM, List.of("My Second Secret Value"))));
    String organizationId2 = organization2.getId();

    // add organization Example 3 with attribute 'secret_attr' with no value
    var organization3 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example3")
                .domains(List.of("example3.com"))
                .url("www.example3.com")
                .displayName("Example 3")
                .attributes(Map.of(CLAIM, List.of(""))));
    String organizationId3 = organization3.getId();

    // add organizanization Example 4 with a different attribute `another_attribute`
    var organization4 =
        createOrganization(
            new OrganizationRepresentation()
                .name("example4")
                .domains(List.of("example4.com"))
                .url("www.example4.com")
                .displayName("Example 4")
                .attributes(Map.of(SECOND_CLAIM, List.of("My Value"))));
    String organizationId4 = organization4.getId();

    // add the user to all organization
    final UserRepresentation user = createUserWithCredentials(keycloak, REALM, "jdoe", "pass");
    List<String> organizationIdList =
        Arrays.asList(organizationId1, organizationId2, organizationId3, organizationId4);
    for (String organizationId : organizationIdList) {
      Response response = putRequest("foo", organizationId, "members", user.getId());
      assertThat(response.getStatusCode(), is(Status.CREATED.getStatusCode()));
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
    validateFirstClaim(
        accessToken, organizationId1, organizationId2, organizationId3, organizationId4);
    validateSecondClaim(accessToken, organizationId4);

    // change authorization
    keycloak =
        getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
    // delete user
    deleteUser(keycloak, REALM, user.getId());

    // delete organization
    for (String organizationId : organizationIdList) {
      deleteOrganization(keycloak, organizationId);
    }
  }

  private void validateSecondClaim(AccessToken accessToken, String organizationId4) {
    Map<String, Object> customClaimValue =
        (Map<String, Object>) accessToken.getOtherClaims().get(SECOND_CLAIM);
    log.debugf("Custom Claim name secret_attr= %s", customClaimValue.toString());

    // check the attribute values
    assertNotNull(customClaimValue);

    // validate organization Example 4 value value is "My Second Secret Value"
    assertThat(customClaimValue.containsKey(organizationId4), is(true));
    assertEquals("My Value", customClaimValue.get(organizationId4));
  }

  private void validateFirstClaim(
      AccessToken accessToken,
      String organizationId1,
      String organizationId2,
      String organizationId3,
      String organizationId4) {
    Map<String, Object> customClaimValue =
        (Map<String, Object>) accessToken.getOtherClaims().get(CLAIM);
    log.debugf("Custom Claim name secret_attr= %s", customClaimValue.toString());

    // check the attribute values
    assertNotNull(customClaimValue);

    // validate organization Example 1 value is "My Secret Value"
    assertThat(customClaimValue.containsKey(organizationId1), is(true));
    assertEquals("My Secret Value", customClaimValue.get(organizationId1));

    // validate organization Example 2 value value is "My Second Secret Value"
    assertThat(customClaimValue.containsKey(organizationId2), is(true));
    assertEquals("My Second Secret Value", customClaimValue.get(organizationId2));

    // validate organization Example 3 value is empty string
    assertThat(customClaimValue.containsKey(organizationId3), is(true));
    assertEquals("", customClaimValue.get(organizationId3));

    // validate organization Example 4 value is not available
    assertThat(customClaimValue.containsKey(organizationId4), is(false));
  }

  private static void configureCustomOidcProtocolMapper(
      RealmResource realm, ClientRepresentation client) {

    // add first claim is same as attribute name
    ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
    mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
    mapper.setProtocolMapper(OrganizationSpecificAttributeMapper.PROVIDER_ID);
    mapper.setName(CLAIM);
    Map<String, String> config = new HashMap<>();
    config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, CLAIM);
    config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
    mapper.setConfig(config);
    realm.clients().get(client.getId()).getProtocolMappers().createMapper(mapper).close();

    // add second claim is same as attribute name
    mapper = new ProtocolMapperRepresentation();
    mapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
    mapper.setProtocolMapper(OrganizationSpecificAttributeMapper.PROVIDER_ID);
    mapper.setName(SECOND_CLAIM);
    config = new HashMap<>();
    config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, SECOND_CLAIM);
    config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
    mapper.setConfig(config);
    realm.clients().get(client.getId()).getProtocolMappers().createMapper(mapper).close();
  }
//
//  protected void createAppClientInRealm(RealmResource realm) {
//    ClientRepresentation client = new ClientRepresentation();
//    client.setClientId("test-app");
//    client.setName("test-app");
//    client.setSecret("password");
//    client.setEnabled(true);
//    client.setDirectAccessGrantsEnabled(true);
//
//    OIDCAdvancedConfigWrapper.fromClientRepresentation(client).setPostLogoutRedirectUris(Collections.singletonList("+"));
//
//    Response response = realm.clients().create(client);
//    response.close();
//  }
}
