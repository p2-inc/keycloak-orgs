package io.phasetwo.service.protocol.oidc.mappers;

import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

@JBossLog
public abstract class AbstractOrganizationMapper extends AbstractOIDCProtocolMapper
    implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

  AbstractOrganizationMapper(
      String providerId,
      String displayType,
      String displayCategory,
      String helpText,
      List<ProviderConfigProperty> config) {
    this.providerId = providerId;
    this.displayType = displayType;
    this.displayCategory = displayCategory;
    this.helpText = helpText;
    this.config = config;
  }

  private final String providerId;
  private final String displayType;
  private final String displayCategory;
  private final String helpText;
  private final List<ProviderConfigProperty> config;

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return config;
  }

  @Override
  public String getId() {
    return providerId;
  }

  @Override
  public String getDisplayType() {
    return displayType;
  }

  @Override
  public String getDisplayCategory() {
    return displayCategory;
  }

  @Override
  public String getHelpText() {
    return helpText;
  }

  protected abstract Map<String, Object> getOrganizationClaim(
      KeycloakSession session, RealmModel realm, UserModel user);

  @Override
  protected void setClaim(
      IDToken token,
      ProtocolMapperModel mappingModel,
      UserSessionModel userSession,
      KeycloakSession keycloakSession,
      ClientSessionContext clientSessionCtx) {
    log.debugf("adding org claim to idToken for %s", userSession.getUser().getUsername());
    Object claim =
        getOrganizationClaim(keycloakSession, userSession.getRealm(), userSession.getUser());
    if (claim == null) return;
    OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claim);
  }

  @Override
  protected void setClaim(
      AccessTokenResponse accessTokenResponse,
      ProtocolMapperModel mappingModel,
      UserSessionModel userSession,
      KeycloakSession keycloakSession,
      ClientSessionContext clientSessionCtx) {
    log.debugf("adding org claim to accessToken for %s", userSession.getUser().getUsername());
    UserModel user = userSession.getUser();
    Object claim =
        getOrganizationClaim(keycloakSession, userSession.getRealm(), userSession.getUser());
    if (claim == null) return;
    OIDCAttributeMapperHelper.mapClaim(accessTokenResponse, mappingModel, claim);
  }
}
