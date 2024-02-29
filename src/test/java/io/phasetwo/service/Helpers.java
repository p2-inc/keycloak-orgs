package io.phasetwo.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.phasetwo.client.openapi.model.WebhookRepresentation;
import io.phasetwo.service.resource.OrganizationResourceType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.representations.idm.AdminEventRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.EventRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.Lists;

public class Helpers {

  private static final ObjectMapper mapper;
  private static List<String> orgsTypes =
      Arrays.stream(OrganizationResourceType.values()).map(Enum::toString).toList();

  static {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static ObjectMapper objectMapper() {
    return mapper;
  }

  public static String toJsonString(Object representation) throws JsonProcessingException {
    return objectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(representation);
  }

  public static UserRepresentation createUser(Keycloak keycloak, String realm, String username) {
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(username);
    return createUser(keycloak, realm, user);
  }

  public static UserRepresentation createUser(
      Keycloak keycloak, String realm, UserRepresentation user) {
    keycloak.realm(realm).users().create(user);
    return keycloak.realm(realm).users().search(user.getUsername()).get(0);
  }

  public static UserRepresentation createUserWithCredentials(
      Keycloak keycloak, String realm, String username, String password) {
    return createUserWithCredentials(keycloak, realm, username, password, null);
  }

  public static UserRepresentation createUserWithCredentials(
      Keycloak keycloak, String realm, String username, String password, String email) {
    CredentialRepresentation pass = new CredentialRepresentation();
    pass.setType(CredentialRepresentation.PASSWORD);
    pass.setValue(password);
    pass.setTemporary(false);
    UserRepresentation user = new UserRepresentation();
    user.setEnabled(true);
    user.setUsername(username);
    if (email != null) {
      user.setEmail(email);
    }
    user.setCredentials(ImmutableList.of(pass));
    return createUser(keycloak, realm, user);
  }

  public static void deleteUser(Keycloak keycloak, String realm, String id) {
    keycloak.realm(realm).users().delete(id);
  }

  public static RealmEventsConfigRepresentation addEventListener(
      Keycloak keycloak, String realm, String name) {
    RealmResource realmResource = keycloak.realm(realm);
    RealmEventsConfigRepresentation eventsConfig = realmResource.getRealmEventsConfig();
    if (eventsConfig.getEventsListeners().contains(name)) return eventsConfig; // disallow dupes
    eventsConfig.setEventsListeners(
        new ImmutableList.Builder<String>()
            .addAll(eventsConfig.getEventsListeners())
            .add(name)
            .build());
    realmResource.updateRealmEventsConfig(eventsConfig);
    return eventsConfig;
  }

  public static RealmEventsConfigRepresentation enableEvents(Keycloak keycloak, String realm) {
    RealmResource realmResource = keycloak.realm(realm);
    RealmEventsConfigRepresentation eventsConfig = realmResource.getRealmEventsConfig();
    eventsConfig.setEventsEnabled(true);
    eventsConfig.setAdminEventsEnabled(Boolean.TRUE);
    eventsConfig.setAdminEventsDetailsEnabled(Boolean.TRUE);
    realmResource.updateRealmEventsConfig(eventsConfig);

    return eventsConfig;
  }

  public static List<EventRepresentation> getEvents(Keycloak keycloak, String realm) {
    RealmResource realmResource = keycloak.realm(realm);
    return realmResource.getEvents();
  }

  public static List<AdminEventRepresentation> getAdminEvents(Keycloak keycloak, String realm) {
    RealmResource realmResource = keycloak.realm(realm);
    return realmResource.getAdminEvents();
  }

  public static void clearAdminEvents(Keycloak keycloak, String realm) {
    RealmResource realmResource = keycloak.realm(realm);
    realmResource.clearAdminEvents();
  }

  public static RealmEventsConfigRepresentation removeEventListener(
      Keycloak keycloak, String realm, String name) {
    RealmResource realmResource = keycloak.realm(realm);
    RealmEventsConfigRepresentation eventsConfig = realmResource.getRealmEventsConfig();
    if (eventsConfig.getEventsListeners().contains(name)) {
      List<String> evs = Lists.newArrayList(eventsConfig.getEventsListeners());
      evs.remove(name);
      eventsConfig.setEventsListeners(evs);
      realmResource.updateRealmEventsConfig(eventsConfig);
    }
    return eventsConfig;
  }

  public static String createWebhook(
      Keycloak keycloak,
      CloseableHttpClient httpClient,
      String baseUrl,
      String url,
      String secret,
      List<String> types)
      throws Exception {
    WebhookRepresentation rep = new WebhookRepresentation();
    rep.setEnabled(true);
    rep.setUrl(url);
    rep.setSecret(secret);
    if (types == null) {
      rep.setEventTypes(List.of("*"));
    } else {
      rep.setEventTypes(types);
    }

    SimpleHttp.Response response =
        SimpleHttp.doPost(baseUrl, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .json(rep)
            .asResponse();
    assertThat(response.getStatus(), is(201));
    assertNotNull(response.getFirstHeader("Location"));
    String loc = response.getFirstHeader("Location");
    String id = loc.substring(loc.lastIndexOf("/") + 1);
    return id;
  }

  public static void deleteWebhook(
      Keycloak keycloak, CloseableHttpClient httpClient, String baseUrl, String webhookId)
      throws Exception {

    SimpleHttp.Response response =
        SimpleHttp.doDelete(baseUrl + "/" + webhookId, httpClient)
            .auth(keycloak.tokenManager().getAccessTokenString())
            .asResponse();
    assertThat(response.getStatus(), is(204));
  }

  public static Stream<AdminEventRepresentation> getOrganizationEvents(Keycloak keycloak) {
    return getAdminEvents(keycloak, "master").stream()
        .filter(
            adminEventRepresentation ->
                orgsTypes.contains(adminEventRepresentation.getResourceType()));
  }
}
