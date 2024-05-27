package io.phasetwo.service.protocol.oidc.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationSpecificAttributeMapper extends AbstractOrganizationMapper {

  public static final String PROVIDER_ID = "oidc-organization-specific-attribute-mapper";

  private static final List<ProviderConfigProperty> configProperties = Lists.newArrayList();

  static {
    OIDCAttributeMapperHelper.addAttributeConfig(
        configProperties, OrganizationSpecificAttributeMapper.class);
  }

  public OrganizationSpecificAttributeMapper() {
    super(
        PROVIDER_ID,
        "Organization Specific Attribute",
        TOKEN_MAPPER_CATEGORY,
        "Map organization single specific attributes in a token claim.",
        configProperties);
  }

  @Override
  protected Map<String, Object> getOrganizationClaim(
      KeycloakSession session, RealmModel realm, UserModel user, ProtocolMapperModel mappingModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    Map<String, Object> organizationClaim = Maps.newHashMap();
    orgs.getUserOrganizationsStream(realm, user)
        .forEach(
            o -> {
              // add to token only when value is available
              String attributeValue = o.getFirstAttribute(mappingModel.getName());
              if (attributeValue != null) {
                organizationClaim.put(o.getId(), attributeValue);
              }
            });
    log.debugf("created user %s organization claim %s", user.getUsername(), organizationClaim);
    return organizationClaim;
  }
}
