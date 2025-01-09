package io.phasetwo.service.broker.saml.mappers;

import static io.phasetwo.service.broker.Mappers.*;
import static org.keycloak.utils.RegexUtils.valueMatchesRegex;

import com.google.auto.service.AutoService;
import io.phasetwo.service.broker.OrgRoleMapper;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

@JBossLog
@AutoService(IdentityProviderMapper.class)
public class AdvancedAttributeToOrgRoleMapper extends AbstractIdentityProviderMapper
    implements OrgRoleMapper {

  public static final String PROVIDER_ID = "saml-advanced-org-role-idp-mapper";

  public static final String[] COMPATIBLE_PROVIDERS = {SAMLIdentityProviderFactory.PROVIDER_ID};

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty attributeMappingProperty = new ProviderConfigProperty();
    attributeMappingProperty.setName(ATTRIBUTE_PROPERTY_NAME);
    attributeMappingProperty.setLabel("Attributes");
    attributeMappingProperty.setHelpText(
        "Name and (regex) value of the attributes to search for in token. "
            + " The configured name of an attribute is searched in SAML attribute name and attribute friendly name fields."
            + " Every given attribute description must be met to set the role."
            + " If the attribute is an array, then the value must be contained in the array."
            + " If an attribute can be found several times, then one match is sufficient.");
    attributeMappingProperty.setType(ProviderConfigProperty.MAP_TYPE);
    configProperties.add(attributeMappingProperty);

    ProviderConfigProperty isAttributeRegexProperty = new ProviderConfigProperty();
    isAttributeRegexProperty.setName(ARE_ATTRIBUTE_VALUES_REGEX_PROPERTY_NAME);
    isAttributeRegexProperty.setLabel("Regex Attribute Values");
    isAttributeRegexProperty.setHelpText(
        "If enabled attribute values are interpreted as regular expressions.");
    isAttributeRegexProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    configProperties.add(isAttributeRegexProperty);

    OrgRoleMapper.addOrgConfigProperties(configProperties);
  }

  @Override
  public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
    return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String[] getCompatibleProviders() {
    return COMPATIBLE_PROVIDERS;
  }

  @Override
  public String getDisplayCategory() {
    return "Role Importer";
  }

  @Override
  public String getDisplayType() {
    return "Advanced Attribute to Org Role";
  }

  @Override
  public String getHelpText() {
    return "If the set of attributes exists and can be matched, grant the user the specified organization role.";
  }

  protected boolean applies(
      final IdentityProviderMapperModel mapperModel, final BrokeredIdentityContext context) {
    Map<String, List<String>> attributes = mapperModel.getConfigMap(ATTRIBUTE_PROPERTY_NAME);
    boolean areAttributeValuesRegexes =
        Boolean.parseBoolean(
            mapperModel
                .getConfig()
                .getOrDefault(ARE_ATTRIBUTE_VALUES_REGEX_PROPERTY_NAME, "false"));

    AssertionType assertion =
        (AssertionType) context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
    Set<AttributeStatementType> attributeAssertions = assertion.getAttributeStatements();
    if (attributeAssertions == null) {
      return false;
    }

    for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
      String attributeKey = entry.getKey();
      for (String value : entry.getValue()) {
        List<Object> attributeValues =
            attributeAssertions.stream()
                .flatMap(statements -> statements.getAttributes().stream())
                .filter(
                    choiceType ->
                        attributeKey.equals(choiceType.getAttribute().getName())
                            || attributeKey.equals(choiceType.getAttribute().getFriendlyName()))
                // Several statements with same name are treated like one with several values
                .flatMap(choiceType -> choiceType.getAttribute().getAttributeValue().stream())
                .collect(Collectors.toList());

        boolean attributeValueMatch =
            areAttributeValuesRegexes
                ? valueMatchesRegex(value, attributeValues)
                : attributeValues.contains(value);
        if (!attributeValueMatch) {
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public void importNewUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    OrganizationModel org = getOrg(session, realm, mapperModel);
    if (org == null) {
      return;
    }
    OrganizationRoleModel role = getOrgRole(session, realm, mapperModel);
    if (role == null) {
      return;
    }

    if (applies(mapperModel, context)) {
      addUserToOrg(org, user, mapperModel);
      grantOrgRole(org, role, user);
    }
  }

  @Override
  public void updateBrokeredUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    OrganizationModel org = getOrg(session, realm, mapperModel);
    if (org == null) {
      return;
    }
    OrganizationRoleModel role = getOrgRole(session, realm, mapperModel);
    if (role == null) {
      return;
    }

    if (applies(mapperModel, context)) {
      addUserToOrg(org, user, mapperModel);
      grantOrgRole(org, role, user);
    } else {
      revokeOrgRole(org, role, user);
    }
  }
}
