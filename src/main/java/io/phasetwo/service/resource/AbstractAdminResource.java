package io.phasetwo.service.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.keycloak.Config;
import org.keycloak.common.ClientConnection;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.resources.admin.permissions.AdminPermissions;

/** */
@JBossLog
@NoCache
public abstract class AbstractAdminResource<T extends AdminAuth> {

  @Context protected ClientConnection clientConnection;
  @Context protected HttpHeaders headers;
  @Context protected KeycloakSession session;

  protected final RealmModel realm;
  protected T auth;
  protected AdminPermissionEvaluator permissions;
  protected AdminEventBuilder adminEvent;
  protected UserModel user;
  protected RealmModel adminRealm;

  protected AbstractAdminResource(RealmModel realm) {
    this.realm = realm;
  }

  protected abstract void init();

  public final void setup() {
    setupAuth();
    setupEvents();
    setupPermissions();
    setupCors();
    init();
  }

  private void setupCors() {
    HttpRequest request = session.getContext().getContextObject(HttpRequest.class);
    HttpResponse response = session.getContext().getContextObject(HttpResponse.class);
    if (hasCors(response)) return;
    Cors.add(request)
        .allowedOrigins(auth.getToken())
        .allowedMethods(CorsResource.METHODS)
        .exposedHeaders("Location")
        .auth()
        .build(response);
  }

  private boolean hasCors(HttpResponse response) {
    MultivaluedMap<String, Object> headers = response.getOutputHeaders();
    if (headers == null) return false;
    return (headers.get("Access-Control-Allow-Credentials") != null
        || headers.get("Access-Control-Allow-Origin") != null
        || headers.get("Access-Control-Expose-Headers") != null);
  }

  private void setupAuth() {

    AppAuthManager authManager = new AppAuthManager();
    String tokenString = authManager.extractAuthorizationHeaderToken(headers);

    if (tokenString == null) {
      throw new NotAuthorizedException("Bearer");
    }

    AccessToken token;

    try {
      JWSInput input = new JWSInput(tokenString);
      token = input.readJsonContent(AccessToken.class);
    } catch (JWSInputException e) {
      throw new NotAuthorizedException("Bearer token format error");
    }

    String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
    RealmManager realmManager = new RealmManager(session);
    adminRealm = realmManager.getRealmByName(realmName);

    if (adminRealm == null) {
      throw new NotAuthorizedException("Unknown realm in token");
    }
    // I think this is fucking up the admin when a user is logged into master and has more than
    // one realm he can administer. it becomes impossible to reason authz for the realm he is
    // administering, because this will always be master. We shouldn't set the session context,
    // or create the AdminAuth with it. TBD if this is true in all circumstances.
    log.debugf(
        "Realm from resource provider is %s. Realm from token is %s",
        this.realm.getName(), adminRealm.getName());
    session.getContext().setRealm(adminRealm);
    AuthenticationManager.AuthResult authResult =
        authenticateBearerToken(
            tokenString,
            session,
            adminRealm,
            session.getContext().getUri(),
            clientConnection,
            headers);
    if (authResult == null) {
      throw new NotAuthorizedException("Bearer");
    }
    session.getContext().setRealm(this.realm);

    ClientModel client =
        adminRealm.getName().equals(Config.getAdminRealm())
            ? this.realm.getMasterAdminClient()
            : this.realm.getClientByClientId(realmManager.getRealmAdminClientId(this.realm));

    if (client == null) {
      throw new NotFoundException("Could not find client for authorization");
    }

    user = authResult.getUser();

    Type genericSuperClass = getClass().getGenericSuperclass();
    ParameterizedType parametrizedType = null;
    while (parametrizedType == null) {
      if ((genericSuperClass instanceof ParameterizedType)) {
        parametrizedType = (ParameterizedType) genericSuperClass;
      } else {
        genericSuperClass = ((Class<?>) genericSuperClass).getGenericSuperclass();
      }
    }

    Class clazz = (Class) parametrizedType.getActualTypeArguments()[0];

    try {
      Constructor<? extends Type> constructor =
          clazz.getConstructor(
              RealmModel.class, AccessToken.class, UserModel.class, ClientModel.class);
      auth = (T) constructor.newInstance(new Object[] {this.realm, token, user, client});
    } catch (NoSuchMethodException
        | SecurityException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException ex) {
      log.error("Failed to instantiate AdminAuth instance", ex);
    }
  }

  private void setupEvents() {
    adminEvent =
        new AdminEventBuilder(this.realm, auth, session, session.getContext().getConnection())
            .realm(realm);
  }

  private void setupPermissions() {
    permissions = AdminPermissions.evaluator(session, realm, adminRealm, user);
  }

  private AuthenticationManager.AuthResult authenticateBearerToken(
      String tokenString,
      KeycloakSession session,
      RealmModel realm,
      UriInfo uriInfo,
      ClientConnection connection,
      HttpHeaders headers) {
    return new AppAuthManager.BearerTokenAuthenticator(session)
        .setRealm(realm)
        .setUriInfo(uriInfo)
        .setTokenString(tokenString)
        .setConnection(connection)
        .setHeaders(headers)
        .authenticate();
  }
}
