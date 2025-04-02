package io.phasetwo.service.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.services.cors.Cors;

@JBossLog
public class CorsResource {
  public static final String[] METHODS = {
    "GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
  };

  public static final String[] HEADERS = {"Location"};

  @OPTIONS
  @Path("{any:.*}")
  public Response preflight() {
    log.debug("CORS OPTIONS preflight request");
    return Cors.builder()
        .preflight()
        .allowedMethods(METHODS)
        .exposedHeaders(HEADERS)
        .auth()
        .add(Response.ok());
  }
}
