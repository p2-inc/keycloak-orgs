package io.phasetwo.service.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.Cors;

@JBossLog
public class CorsResource {

  private final KeycloakSession session;
  private final HttpRequest request;

  public CorsResource(KeycloakSession session, HttpRequest request) {
    this.session = session;
    this.request = request;
  }

  public static final String[] METHODS = {
    "GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
  };

  @OPTIONS
  @Path("{any:.*}")
  public Response preflight() {
    log.debug("CORS OPTIONS preflight request");
    return Cors.add(request, Response.ok()).auth().allowedMethods(METHODS).preflight().build();
  }
}
