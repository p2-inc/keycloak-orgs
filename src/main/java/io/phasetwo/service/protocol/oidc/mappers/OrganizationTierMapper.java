package io.phasetwo.service.protocol.oidc.mappers;

import static org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper.addIncludeInTokensConfig;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
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
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.representations.IDToken;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationTierMapper extends AbstractOrganizationMapper {

  public static final String PROVIDER_ID = "oidc-organization-tier-mapper";

  private static final List<ProviderConfigProperty> configProperties = Lists.newArrayList();

  static {
    addIncludeInTokensConfig(configProperties, OrganizationTierMapper.class);
  }

  public OrganizationTierMapper() {
    super(
        PROVIDER_ID,
        "Organization Tier to Access",
        TOKEN_MAPPER_CATEGORY,
        "Append organizations tiers to access token realm_access.roles",
        configProperties);
  }

  @Override
  protected Map<String, Object> getOrganizationClaim(KeycloakSession session, RealmModel realm,
      UserModel user, ProtocolMapperModel mapperModel) {
    return Map.of();
  }

  @Override
  protected void setClaim(
      IDToken idToken,
      ProtocolMapperModel mappingModel,
      UserSessionModel userSession,
      KeycloakSession session,
      ClientSessionContext clientSessionCtx) {
    log.debugf("adding orgs tiers to accessToken for %s", userSession.getUser().getUsername());

    if (!(idToken instanceof AccessToken token)) {
      return;
    }

    Access access = token.getRealmAccess();
    if (access == null) {
      access = new AccessToken.Access();
      token.setRealmAccess(access);
    }

    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    Access finalAccess = access;
    orgs.getUserOrganizationsStream(userSession.getRealm(), userSession.getUser())
        .forEach(o -> {
          List<String> realmLevelTiers = o.getRealmTierMappingsStream()
              .map(t -> t.getRole().getName()).toList();

          for(String role : realmLevelTiers) {
            finalAccess.addRole(role);
          }
        });
  }
}
