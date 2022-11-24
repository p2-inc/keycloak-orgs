package io.phasetwo.service.protocol.oidc.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationAttributeMapper extends AbstractOrganizationMapper {

  public static final String PROVIDER_ID = "oidc-organization-attribute-mapper";

  private static final List<ProviderConfigProperty> configProperties = Lists.newArrayList();

  static {
    OIDCAttributeMapperHelper.addAttributeConfig(
        configProperties, OrganizationAttributeMapper.class);
  }

  public OrganizationAttributeMapper() {
    super(
        PROVIDER_ID,
        "Organization Attribute",
        TOKEN_MAPPER_CATEGORY,
        "Map organization attributes in a token claim.",
        configProperties);
  }

  @Override
  protected Map<String, Object> getOrganizationClaim(
      KeycloakSession session, RealmModel realm, UserModel user) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    Map<String, Object> claim = Maps.newHashMap();
    orgs.getUserOrganizationsStream(realm, user)
        .forEach(
            o -> {
              Map<String, Object> org = Maps.newHashMap();
              org.put("name", o.getName());
              org.put("attributes", o.getAttributes());
              claim.put(o.getId(), org);
            });
    log.debugf("created user %s claim %s", user.getUsername(), claim);
    return claim;
  }
}
