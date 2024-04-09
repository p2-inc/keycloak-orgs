package io.phasetwo.service.protocol.oidc.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.phasetwo.service.util.ActiveOrganization;
import java.util.Arrays;
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
public class ActiveOrganizationMapper extends AbstractOrganizationMapper {

  public static final String PROVIDER_ID = "oidc-active-organization-mapper";
  public static final String INCLUDED_ORGANIZATION_PROPERTIES =
      "included.active.organization.properties";
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String ROLE = "role";
  private static final String ATTRIBUTE = "attribute";

  private static final List<ProviderConfigProperty> configProperties = Lists.newArrayList();

  static {
    ProviderConfigProperty property = new ProviderConfigProperty();
    property.setName(INCLUDED_ORGANIZATION_PROPERTIES);
    property.setLabel("Active Organization Properties");
    property.setHelpText(
        "Properties of the active organization to map into the token claims, "
            + "it can be multiple, separated by comma. Available properties are: id, name, role and attribute. "
            + "For example you can write: id or id, role");
    property.setType(ProviderConfigProperty.STRING_TYPE);
    property.setDefaultValue("id, name, role, attribute");
    configProperties.add(property);

    OIDCAttributeMapperHelper.addAttributeConfig(configProperties, ActiveOrganizationMapper.class);
  }

  public ActiveOrganizationMapper() {
    super(
        PROVIDER_ID,
        "Active Organization",
        TOKEN_MAPPER_CATEGORY,
        "Map active organization properties in a token claim.",
        configProperties);
  }

  @Override
  protected Map<String, Object> getOrganizationClaim(
      KeycloakSession session, RealmModel realm, UserModel user, ProtocolMapperModel mappingModel) {
    ActiveOrganization activeOrganizationUtil = ActiveOrganization.fromContext(session, realm, user);

    if (!activeOrganizationUtil.isValid()) {
      return Maps.newHashMap();
    }

    String inputProperties = mappingModel.getConfig().get(INCLUDED_ORGANIZATION_PROPERTIES);
    List<String> properties = Arrays.asList(inputProperties.replaceAll("\\s", "")
        .split(","));

    Map<String, Object> claim = Maps.newHashMap();
    if (properties.contains(ID)) {
      claim.put(ID, activeOrganizationUtil.getOrganization().getId());
    }

    if (properties.contains(NAME)) {
      claim.put(NAME, activeOrganizationUtil.getOrganization().getName());
    }

    if (properties.contains(ROLE)) {
      claim.put(ROLE, activeOrganizationUtil.getUserActiveOrganizationRoles());
    }

    if (properties.contains(ATTRIBUTE)) {
      claim.put(ATTRIBUTE, activeOrganizationUtil.getOrganization().getAttributes());
    }

    log.debugf("created user %s claim %s", user.getUsername(), claim);
    return claim;
  }
}
