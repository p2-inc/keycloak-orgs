package io.phasetwo.wizard;

import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.PathSegment;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.Version;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.theme.Theme;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.RealmModel;
import java.net.URI;
import java.net.URLConnection;
import javax.activation.MimetypesFileTypeMap;
import java.lang.reflect.Method;
import java.util.Locale;

@JBossLog
public class WizardResourceProvider implements RealmResourceProvider {

  private KeycloakSession session;

  public WizardResourceProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public Object getResource() {
    return this;
  }

  /**
   * awful hack version for keycloak-x
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response wizard() {
    String wizardResources = ".";
    Theme theme = getTheme("wizard");
    RealmModel realm = session.getContext().getRealm();
    LoginFormsProvider form = session.getProvider(LoginFormsProvider.class).setAttribute("wizardResources", wizardResources).setAttribute("realmName", realm.getName());
    FreeMarkerLoginFormsProvider fm = (FreeMarkerLoginFormsProvider)form;
    try {
      Method processTemplateMethod = fm.getClass().getDeclaredMethod("processTemplate", Theme.class, String.class, Locale.class);
      processTemplateMethod.setAccessible(true);
      Response resp = (Response) processTemplateMethod.invoke(fm, theme, "wizard.ftl", session.getContext().resolveLocale(null));
      return resp;
    } catch (Exception e) {
      log.warn("Could not call processTemplate on FreeMarkerLoginFormsProvider", e);
    }
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }
  
  /* this version isn't working in Keycloak-X
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response wizard() {
    String wizardResources = ".";
    log.debugf("wizardResources %s", wizardResources);
    RealmModel realm = session.getContext().getRealm();
    return session.getProvider(LoginFormsProvider.class).setAttribute("wizardResources", wizardResources).setAttribute("realmName", realm.getName()).createForm("wizard.ftl");
  }
  */
  
  @GET
  @Path("{path: ^(200|fonts|images|main|site).*}")
  public Response staticResources(@PathParam("path") final String path) throws IOException{
    String fileName = getLastPathSegment(session.getContext().getUri());
    Theme theme = getTheme("wizard");
    InputStream resource = theme.getResourceAsStream(path);
    String mimeType = getMimeType(fileName);
    log.infof("%s [%s] (%s)", path, mimeType, null==resource ? "404" : "200");
    return null == resource
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok(resource, mimeType).build();
  }
  
  @GET
  @Path("/keycloak.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response keycloakJson() {
    RealmModel realm = session.getContext().getRealm();
    UriInfo uriInfo = session.getContext().getUri();
    Map json = ImmutableMap.of("realm", realm.getName(),
                               "auth-server-url", getBaseUrl(uriInfo),
                               "resource", "idp-wizard");
    return Response.ok(json).build();
  }

  private static String getBaseUrl(UriInfo uriInfo) {
    String u = uriInfo.getBaseUri().toString();
    if (u != null && u.endsWith("/")) u = u.substring(0, u.length() - 1);
    return u;
  }
    
  /*
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response wizard() {
    Theme theme = getTheme("wizard");
    UriInfo uriInfo = session.getContext().getUri();
    log.infof("absolutePath %s", uriInfo.getAbsolutePath());
    log.infof("path %s", uriInfo.getPath());
    log.infof("baseUri %s", uriInfo.getBaseUri());
    UriBuilder uriBuilder =
        UriBuilder.fromUri(uriInfo.getBaseUri())
        .path("resources")
        .path(Version.RESOURCES_VERSION)
        .path(theme.getType().toString().toLowerCase())
        .path(theme.getName());

    URI resourcePath = uriBuilder.build();
    log.infof("resourcePath %s", resourcePath.toString());
    String wizardResources = uriInfo.relativize(resourcePath).toString();
    log.infof("wizardResources %s", wizardResources);
    RealmModel realm = session.getContext().getRealm();
    return session.getProvider(LoginFormsProvider.class).setAttribute("wizardResources", wizardResources).setAttribute("realmName", realm.getName()).createForm("wizard.ftl");
  }
  */
  
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

  private static final String MIME_TYPES = "text/javascript js\n" +
      "text/css css\n" +
      "font/woff woff\n" +
      "font/woff2 woff2\n" +
      "application/json json webmanifest map\n" +
      "image/svg+xml svg\n";

  private String getMimeType(String fileName) {
    MimetypesFileTypeMap map = new MimetypesFileTypeMap(); 
    map.addMimeTypes(MIME_TYPES); // loading from mime.types on classpath not working
    return map.getContentType(fileName);
  }

  @Override
  public void close() {}

}
