package io.phasetwo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URLEncoder;

public class Helpers {

  public static String toJsonString(Object representation) throws JsonProcessingException {
    return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(representation);
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

  private static boolean isLocalPortFree(int port) {
    try {
      new ServerSocket(port).close();
      return true;
    } catch (IOException e) {
      return false;
    }
  }
}
