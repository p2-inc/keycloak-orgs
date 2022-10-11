package io.phasetwo.service.resource;

import com.google.common.collect.Maps;
import com.google.common.io.CharSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.theme.Theme;

@JBossLog
public class AssetsResourceProvider implements RealmResourceProvider {

  private KeycloakSession session;

  public AssetsResourceProvider(KeycloakSession session) {
    this.session = session;
  }

  public static final String ASSETS_LOGIN_CSS_PREFIX = "_providerConfig.assets.login.css";
  public static final String ASSETS_LOGIN_LOGO_URL = "_providerConfig.assets.logo.url";
  public static final String ASSETS_LOGIN_FAVICON_URL = "_providerConfig.assets.favicon.url";

  @Override
  public Object getResource() {
    return this;
  }

  @GET
  @Path("css/login.css")
  @Produces("text/css")
  public Response cssLogin(@Context HttpHeaders headers, @Context UriInfo uriInfo)
      throws IOException {
    String css = session.getContext().getRealm().getAttribute(ASSETS_LOGIN_CSS_PREFIX);
    if (css == null) {
      StringBuilder o = new StringBuilder("/* login css */\n");
      o.append(":root {\n");
      session
          .getContext()
          .getRealm()
          .getAttributes()
          .forEach(
              (k, v) -> {
                if (k.startsWith(ASSETS_LOGIN_CSS_PREFIX)) {
                  String name = k.substring(ASSETS_LOGIN_CSS_PREFIX.length() + 1);
                  o.append("  --").append(name).append(": ").append(v).append(";\n");
                }
              });
      o.append("}\n");
      css = o.toString();
    }
    InputStream resource = CharSource.wrap(css).asByteSource(StandardCharsets.UTF_8).openStream();
    String mimeType = "text/css";
    return null == resource
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok(resource, mimeType).build();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////////////////////////////

  @GET
  @Path("/debug")
  @Produces(MediaType.APPLICATION_JSON)
  public Response debug(@Context HttpHeaders headers, @Context UriInfo uriInfo) {
    log.infof("query string %s", uriInfo.getRequestUri().getQuery());
    Map<String, Object> m = Maps.newHashMap();
    m.put("base_uri", uriInfo.getBaseUri().toString());
    m.put("request_uri", uriInfo.getRequestUri().toString());
    m.put("query_string", uriInfo.getRequestUri().getQuery());
    m.put("path", uriInfo.getPath());
    Map<String, List<String>> h = headers.getRequestHeaders();
    m.put("headers", h);
    return Response.ok().entity(m).build();
  }

  /*
  @GET
  public Response portal() {
    Theme theme = getTheme();
    UriInfo uriInfo = session.getContext().getUri();
    UriBuilder uriBuilder =
        UriBuilder.fromUri(uriInfo.getBaseUri())
            .path("resources")
            .path(Version.RESOURCES_VERSION)
            .path(theme.getType().toString().toLowerCase())
            .path(theme.getName())
            .path("index.html");
    return Response.temporaryRedirect(uriBuilder.build()).build();
  }
  */

  private Theme getCurrentLoginTheme() {
    try {
      return session.theme().getTheme(Theme.Type.LOGIN);
    } catch (IOException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void close() {}
}
