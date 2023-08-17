package io.phasetwo.service;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.xgp.http.server.Server;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.phasetwo.client.openapi.model.WebhookRepresentation;
import io.phasetwo.service.representation.OrganizationRole;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.impl.client.CloseableHttpClient;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.representations.idm.RealmEventsConfigRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class Helpers {

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

  public static void deleteUser(Keycloak keycloak, String realm, String id) {
    keycloak.realm(realm).users().delete(id);
  }

  public static String urlencode(String u) {
    try {
      return URLEncoder.encode(u, "UTF-8");
    } catch (Exception e) {
      return "";
    }
  }

  public static int nextFreePort(int from, int to) {
    for (int port = from; port <= to; port++) {
      if (isLocalPortFree(port)) {
        return port;
      }
    }
    throw new IllegalStateException("No free port found");
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
          int port,
          String baseUrl,
          List<String> types,
          Callable<Void> sendEvents,
          Consumer<ArrayList<String>> consumeResult
          ) throws Exception{
    ArrayList<String> webhookResponses = new ArrayList<String>();

    String webhookId = createWebhook(
            keycloak,
            httpClient,
            baseUrl,
            "http://127.0.0.1:" + port + "/webhook",
            "qlfwemke",
            types);
    Server srv = new Server(port);
    srv
            .router()
            .POST(
                    "/webhook",
                    (request, resp) -> {
                      String r = request.body();
//                      log.infof("server webhook: %s", r);
                      webhookResponses.add(r);
                      resp.body("OK");
                      resp.status(202);
                    });
    srv.start();
    Thread.sleep(1000l);

    sendEvents.call();

    Thread.sleep(1000l);

    deleteWebhook(keycloak, httpClient, baseUrl, webhookId);
    srv.stop();
    consumeResult.accept(webhookResponses);
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
