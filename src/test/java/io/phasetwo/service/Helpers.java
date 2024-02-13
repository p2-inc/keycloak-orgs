package io.phasetwo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.phasetwo.client.openapi.model.WebhookRepresentation;
import lombok.extern.jbosslog.JBossLog;

import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertNotNull;

import com.github.xgp.http.server.Server;

@JBossLog
public class Helpers {

  private static final ObjectMapper mapper;

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
          Keycloak keycloak,
          CloseableHttpClient httpClient,
          String baseUrl,
          String webhookId)
          throws Exception {

    SimpleHttp.Response response =
            SimpleHttp.doDelete(baseUrl+"/"+webhookId, httpClient)
                    .auth(keycloak.tokenManager().getAccessTokenString())
                    .asResponse();
    assertThat(response.getStatus(), is(204));
  }

  public static void webhookTestWrapper(
          Keycloak keycloak,
          CloseableHttpClient httpClient,
          String baseUrl,
          int port,
          List<String> types,
          Callable<Void> sendEvents,
          Consumer<ArrayList<String>> consumeResult) throws Exception {

    addEventListener(keycloak, "master", "ext-event-webhook");

    ArrayList<String> webhookResponses = new ArrayList<String>();

    Server srv = new Server(port);
    srv
        .router()
        .POST(
            "/webhook",
            (request, resp) -> {
              String b = request.body();
              log.infof("webhook: body %s", b);
              webhookResponses.add(b);
              resp.body("OK");
              resp.status(200);
            });

    srv.start();
    log.info("webhook: srv.start()");

    String webhookId = createWebhook(
        keycloak,
        httpClient,
        baseUrl,
        "http://host.testcontainers.internal:" + port + "/webhook",
        "qlfwemke",
        types);

    Thread.sleep(1000l);

    sendEvents.call();

    Thread.sleep(2500l);

    removeEventListener(keycloak, "master", "ext-event-webhook");

    srv.stop();
    log.info("webhook: srv.stop()");

    deleteWebhook(keycloak, httpClient, baseUrl, webhookId);

    consumeResult.accept(webhookResponses);
  }

  public static int nextFreePort(int from, int to) {
    for (int port = from; port <= to; port++) {
      if (isLocalPortFree(port)) {
        return port;
      }
    }
    throw new IllegalStateException("No free port found");
  }

  private static boolean isLocalPortFree(int port) {
    try {
      new ServerSocket(port).close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
