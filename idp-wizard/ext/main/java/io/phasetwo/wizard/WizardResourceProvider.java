package io.phasetwo.wizard;

import com.google.common.collect.ImmutableMap;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.Cors;
import org.keycloak.theme.Theme;

@JBossLog
public class WizardResourceProvider implements RealmResourceProvider {

  private final KeycloakSession session;
  private final String authRealmOverride;

  public WizardResourceProvider(KeycloakSession session, String authRealmOverride) {
    this.session = session;
    this.authRealmOverride = authRealmOverride;
  }

  @Override
  public Object getResource() {
    return this;
  }

  private void setupCors() {
    HttpRequest request = session.getContext().getHttpRequest();
    HttpResponse response = session.getContext().getHttpResponse();
    UriInfo uriInfo = session.getContext().getUri();
    Cors.add(request).allowAllOrigins().allowedMethods(METHODS).auth().build(response);
  }

  public static final String[] METHODS = {
    "GET", "HEAD", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
  };

  @OPTIONS
  @Path("{any:.*}")
  public Response preflight() {
    log.trace("CORS OPTIONS preflight request");
    HttpRequest request = session.getContext().getContextObject(HttpRequest.class);
    return Cors.add(request, Response.ok()).auth().allowedMethods(METHODS).preflight().build();
  }

  /**
   * something is seriously wrong since resteasy reactive upgrade. catch-all and regex paths no
   * longer work from annotations. This is a hack to get v23 working in the meantime.
   */
  @GET
  @Path("{path:.+}")
  public Response router(@PathParam("path") String path) throws Exception {
    UriInfo uriInfo = session.getContext().getUri();
    log.tracef("param path %s", path);
    log.tracef("absolutePath %s", uriInfo.getAbsolutePath());
    log.tracef("path %s", uriInfo.getPath());
    log.tracef("baseUri %s", uriInfo.getBaseUri());
    log.tracef("segments %s", uriInfo.getPathSegments());
    log.tracef("requestUri %s", uriInfo.getRequestUri());

    if (path == null || "".equals(path) || "/".equals(path)) return wizard();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }

    if (Pattern.matches("^(200|fonts|images|main|site).*", path)) {
      Response response = staticResources(path);
      if (response != null) {
        log.tracef("returning response %d for path %s", response.getStatus(), path);
        return response;
      }
    }

    return wizard();
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response wizard() {
    log.trace("wizard index file");
    String wizardResources = ".";
    Theme theme = getTheme("wizard");
    RealmModel realm = session.getContext().getRealm();
    LoginFormsProvider form =
        session
            .getProvider(LoginFormsProvider.class)
            .setAttribute("wizardResources", wizardResources)
            .setAttribute("realmName", realm.getName());
    FreeMarkerLoginFormsProvider fm = (FreeMarkerLoginFormsProvider) form;
    try {
      Method processTemplateMethod =
          fm.getClass()
              .getDeclaredMethod("processTemplate", Theme.class, String.class, Locale.class);
      processTemplateMethod.setAccessible(true);
      Response resp =
          (Response)
              processTemplateMethod.invoke(
                  fm, theme, "wizard.ftl", session.getContext().resolveLocale(null));
      return resp;
    } catch (Exception e) {
      log.warn("Could not call processTemplate on FreeMarkerLoginFormsProvider", e);
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  @GET
  @Path("{path:^(200|fonts|images|main|site).*}")
  public Response staticResources(@PathParam("path") final String path) throws IOException {
    log.trace("static resources");
    String fileName = getLastPathSegment(session.getContext().getUri());
    Theme theme = getTheme("wizard");
    InputStream resource = theme.getResourceAsStream(path);
    String mimeType = getMimeType(fileName);
    log.tracef("%s [%s] (%s)", path, mimeType, null == resource ? "404" : "200");
    return null == resource
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok(resource, mimeType).build();
  }

  @GET
  @Path("keycloak.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response keycloakJson() {
    log.trace("keycloak.json");
    setupCors();
    RealmModel realm = session.getContext().getRealm();
    UriInfo uriInfo = session.getContext().getUri();
    Map json =
        ImmutableMap.of(
            "realm",
            authRealmOverride,
            "auth-server-url",
            getBaseUrl(uriInfo),
            "resource",
            "idp-wizard");
    return Response.ok(json).build();
  }

  @GET
  @Path("config.json")
  @Produces(MediaType.APPLICATION_JSON)
  public WizardConfig configJson() {
    log.trace("config.json");
    setupCors();
    return WizardConfig.createFromAttributes(session);
  }

  private static String getBaseUrl(UriInfo uriInfo) {
    String u = uriInfo.getBaseUri().toString();
    if (u != null && u.endsWith("/")) u = u.substring(0, u.length() - 1);
    return u;
  }

  private static String getOrigin(UriInfo uriInfo) {
    return uriInfo.getBaseUri().resolve("/").toString();
  }

  private Theme getTheme(String name) {
    try {
      return session.theme().getTheme(name, Theme.Type.LOGIN);
    } catch (IOException e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  private String getLastPathSegment(UriInfo uriInfo) {
    List<PathSegment> segs = uriInfo.getPathSegments();
    if (segs != null && segs.size() > 0) {
      return segs.get(segs.size() - 1).getPath();
    }
    return null;
  }

  private static final String MIME_TYPES =
      "text/javascript js\n"
          + "text/css css\n"
          + "font/woff woff\n"
          + "font/woff2 woff2\n"
          + "application/json json webmanifest map\n"
          + "image/svg+xml svg\n";

  private String getMimeType(String fileName) {
    MimetypesFileTypeMap map = new MimetypesFileTypeMap();
    map.addMimeTypes(MIME_TYPES); // loading from mime.types on classpath not working
    return map.getContentType(fileName);
  }

  @Override
  public void close() {}
}
