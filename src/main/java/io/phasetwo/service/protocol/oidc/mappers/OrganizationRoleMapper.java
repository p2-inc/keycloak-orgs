package io.phasetwo.service.protocol.oidc.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.IDToken;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationRoleMapper extends AbstractOIDCProtocolMapper
    implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

  private static final List<ProviderConfigProperty> configProperties = Lists.newArrayList();

  public static final String PROVIDER_ID = "oidc-organization-role-mapper";

  static {
    OIDCAttributeMapperHelper.addAttributeConfig(configProperties, OrganizationRoleMapper.class);
  }

  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayType() {
    return "Organization Role";
  }

  @Override
  public String getDisplayCategory() {
    return TOKEN_MAPPER_CATEGORY;
  }

  @Override
  public String getHelpText() {
    return "Map members and roles to organizations in a token claim.";
  }

  /*
   organizations: [
     foo: [
       "admin"
          ],
     bar: []
   ]
   gets all the roles for each organization of which the user is a member
  */
  private Map<String, Object> getOrganizationRoleClaim(
      KeycloakSession session, RealmModel realm, UserModel user) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    Map<String, Object> claim = Maps.newHashMap();
    orgs.getUserOrganizationsStream(realm, user)
        .forEach(
            o -> {
              List<String> roles = Lists.newArrayList();
              o.getRolesStream()
                  .forEach(
                      r -> {
                        if (r.hasRole(user)) roles.add(r.getName());
                      });
              Map<String, Object> org = Maps.newHashMap();
              org.put("name", o.getName());
              org.put("roles", roles);
              claim.put(o.getId(), org);
            });
    log.debugf("created user %s claim %s", user.getUsername(), claim);
    return claim;
  }

  @Override
  protected void setClaim(
      IDToken token,
      ProtocolMapperModel mappingModel,
      UserSessionModel userSession,
      KeycloakSession keycloakSession,
      ClientSessionContext clientSessionCtx) {
    log.debugf("adding org claim to idToken for %s", userSession.getUser().getUsername());
    Map<String, Object> claim =
        getOrganizationRoleClaim(keycloakSession, userSession.getRealm(), userSession.getUser());
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
    Map<String, Object> claim =
        getOrganizationRoleClaim(keycloakSession, userSession.getRealm(), userSession.getUser());
    if (claim == null) return;
    OIDCAttributeMapperHelper.mapClaim(accessTokenResponse, mappingModel, claim);
  }
}
