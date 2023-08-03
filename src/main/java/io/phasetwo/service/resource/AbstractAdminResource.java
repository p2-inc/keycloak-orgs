package io.phasetwo.service.resource;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.Config;
import org.keycloak.common.ClientConnection;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
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

  protected final ClientConnection connection;
  protected final HttpHeaders headers;
  protected final KeycloakSession session;
  protected final RealmModel realm;

  protected T auth;
  protected AdminPermissionEvaluator permissions;
  protected AdminEventBuilder adminEvent;
  protected UserModel user;
  protected RealmModel adminRealm;

  protected AbstractAdminResource(KeycloakSession session) {
    this.session = session;
    this.realm = session.getContext().getRealm();
    this.headers = session.getContext().getRequestHeaders();
    this.connection = session.getContext().getConnection();
  }

  protected AbstractAdminResource(AbstractAdminResource<T> parent) {
    this.connection = parent.connection;
    this.headers = parent.headers;
    this.session = parent.session;
    this.realm = parent.realm;
    this.auth = parent.auth;
    this.permissions = parent.permissions;
    this.adminEvent = parent.adminEvent;
    this.user = parent.user;
    this.adminRealm = parent.adminRealm;
  }

  public final void setup() {
    setupAuth();
    setupEvents();
    setupPermissions();
    setupCors();
  }

  private void setupCors() {
    HttpRequest request = session.getContext().getHttpRequest();
    HttpResponse response = session.getContext().getHttpResponse();
    Cors.add(request)
        .allowedOrigins(auth.getToken())
        .allowedMethods(CorsResource.METHODS)
        .exposedHeaders("Location")
        .auth()
        .build(response);
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
            tokenString, session, adminRealm, session.getContext().getUri(), connection, headers);
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
