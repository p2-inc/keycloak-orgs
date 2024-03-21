package io.phasetwo.service.util;

import jakarta.ws.rs.NotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager.AccessTokenResponseBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.util.MtlsHoKTokenUtil;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.RootAuthenticationSessionModel;

public class TokenManager {

  private final KeycloakSession session;
  private final AccessToken accessToken;
  private final RealmModel realm;
  private final ClientModel targetClient;
  private final OIDCAdvancedConfigWrapper targetClientConfig;
  private final UserModel user;

  public TokenManager(
      KeycloakSession session, AccessToken accessToken, RealmModel realm, UserModel user) {
    this.accessToken = accessToken;
    this.realm = realm;
    this.targetClient =
        session
            .getProvider(ClientProvider.class)
            .getClientByClientId(realm, accessToken.getIssuedFor());
    this.targetClientConfig = OIDCAdvancedConfigWrapper.fromClientModel(targetClient);
    this.user = user;
    this.session = session;
    this.session.getContext().setClient(targetClient);
  }

  /**
   * Generate a new access token, refresh token and id token with an updated state and based on the
   * previous access token.
   *
   * @return a {@link AccessTokenResponse}
   */
  public AccessTokenResponse generateTokens() {
    // Create new authSession with Default + Optional Scope matching old token
    AuthenticationSessionModel authSession = getAuthSession(getScopeIds());

    EventBuilder event =
        new EventBuilder(
            session.getContext().getRealm(), session, session.getContext().getConnection());
    ClientSessionContext clientSessionCtx =
        AuthenticationProcessor.attachSession(
            authSession, null, session, realm, session.getContext().getConnection(), event);
    UserSessionModel userSession = clientSessionCtx.getClientSession().getUserSession();

    // Generate new token
    org.keycloak.protocol.oidc.TokenManager tokenManager =
        new org.keycloak.protocol.oidc.TokenManager();
    AccessTokenResponseBuilder responseBuilder =
        tokenManager
            .responseBuilder(realm, targetClient, event, session, userSession, clientSessionCtx)
            .generateAccessToken();
    // rewrite audience and allowed origin based on previous token
    responseBuilder.getAccessToken().audience(accessToken.getAudience());
    responseBuilder.getAccessToken().setAllowedOrigins(accessToken.getAllowedOrigins());

    boolean useRefreshToken = targetClientConfig.isUseRefreshToken();
    if (useRefreshToken) {
      responseBuilder.generateRefreshToken();
    }

    String scopeParam = clientSessionCtx.getClientSession().getNote(OAuth2Constants.SCOPE);
    if (org.keycloak.util.TokenUtil.isOIDCRequest(scopeParam)) {
      responseBuilder.generateIDToken().generateAccessTokenHash();
    }

    checkAndBindMtlsHoKToken(event, responseBuilder, useRefreshToken);

    return responseBuilder.build();
  }

  // Extracted from Keycloak in TokenEndpoint
  private void checkAndBindMtlsHoKToken(
      EventBuilder event, AccessTokenResponseBuilder responseBuilder, boolean useRefreshToken) {
    // KEYCLOAK-6771 Certificate Bound Token
    // https://tools.ietf.org/html/draft-ietf-oauth-mtls-08#section-3
    if (targetClientConfig.isUseMtlsHokToken()) {
      AccessToken.Confirmation confirmation =
          MtlsHoKTokenUtil.bindTokenWithClientCertificate(
              session.getContext().getHttpRequest(), session);
      if (confirmation != null) {
        responseBuilder.getAccessToken().setConfirmation(confirmation);
        if (useRefreshToken) {
          responseBuilder.getRefreshToken().setConfirmation(confirmation);
        }
      } else {
        event.error(Errors.INVALID_REQUEST);
        throw new NotFoundException("Client Certification missing for MTLS HoK Token Binding");
      }
    }
  }

  private Set<String> getScopeIds() {
    // Get all default scopes
    Map<String, ClientScopeModel> defaultClientScopes = targetClient.getClientScopes(true);
    // Get all optional scopes
    Map<String, ClientScopeModel> optionalClientScopes = targetClient.getClientScopes(false);
    Set<String> clientScopeIds =
        defaultClientScopes.values().stream()
            .map(ClientScopeModel::getId)
            .collect(Collectors.toSet());

    // Add optional scopes only if part of previous token
    Set<String> accessTokenScopes = Set.of(accessToken.getScope().split(" "));
    optionalClientScopes.values().stream()
        .filter(cs -> accessTokenScopes.contains(cs.getName()))
        .map(ClientScopeModel::getId)
        .forEach(clientScopeIds::add);

    return clientScopeIds;
  }

  private AuthenticationSessionModel getAuthSession(Set<String> clientScopeIds) {
    RootAuthenticationSessionModel rootAuthSession =
        new AuthenticationSessionManager(session).createAuthenticationSession(realm, false);
    AuthenticationSessionModel authSession =
        rootAuthSession.createAuthenticationSession(targetClient);

    authSession.setAuthenticatedUser(user);
    authSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
    authSession.setClientNote(
        OIDCLoginProtocol.ISSUER,
        Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
    authSession.setClientNote(OIDCLoginProtocol.SCOPE_PARAM, accessToken.getScope());
    authSession.setClientScopes(clientScopeIds);

    return authSession;
  }
}
