package io.phasetwo.service.broker.oidc.mappers;

import static org.keycloak.utils.RegexUtils.valueMatchesRegex;
import static io.phasetwo.service.broker.Mappers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.ConfigConstants;
import org.keycloak.broker.saml.SAMLEndpoint;
import org.keycloak.broker.saml.SAMLIdentityProviderFactory;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;

public class AdvancedAttributeToOrgRoleMapper extends AbstractIdentityProviderMapper {

    public static final String PROVIDER_ID = "saml-advanced-org-role-idp-mapper";

    public static final String[] COMPATIBLE_PROVIDERS = {
            SAMLIdentityProviderFactory.PROVIDER_ID
    };

    private static final List<ProviderConfigProperty> configProperties =
            new ArrayList<>();

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
        isAttributeRegexProperty.setHelpText("If enabled attribute values are interpreted as regular expressions.");
        isAttributeRegexProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        configProperties.add(isAttributeRegexProperty);

        addOrgConfigProperties(configProperties);
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

    protected boolean applies(final IdentityProviderMapperModel mapperModel, final BrokeredIdentityContext context) {
        Map<String, List<String>> attributes = mapperModel.getConfigMap(ATTRIBUTE_PROPERTY_NAME);
        boolean areAttributeValuesRegexes = Boolean.parseBoolean(mapperModel.getConfig().get(ARE_ATTRIBUTE_VALUES_REGEX_PROPERTY_NAME));

        AssertionType assertion = (AssertionType) context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
        Set<AttributeStatementType> attributeAssertions = assertion.getAttributeStatements();
        if (attributeAssertions == null) {
            return false;
        }

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            String attributeKey = entry.getKey();
            for (String value : entry.getValue()) {
                List<Object> attributeValues = attributeAssertions.stream()
                        .flatMap(statements -> statements.getAttributes().stream())
                        .filter(choiceType -> attributeKey.equals(choiceType.getAttribute().getName())
                        || attributeKey.equals(choiceType.getAttribute().getFriendlyName()))
                        // Several statements with same name are treated like one with several values
                        .flatMap(choiceType -> choiceType.getAttribute().getAttributeValue().stream())
                        .collect(Collectors.toList());

                boolean attributeValueMatch = areAttributeValuesRegexes ? valueMatchesRegex(value, attributeValues) : attributeValues.contains(value);
                if (!attributeValueMatch) {
                    return false;
                }
            }
        }

        return true;
    }


    @Override
    public void importNewUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = this.getRole(realm, mapperModel);
        if (role == null) {
            return;
        }

        if (this.applies(mapperModel, context)) {
            user.grantRole(role);
        }
    }

    @Override
    public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
        RoleModel role = this.getRole(realm, mapperModel);
        if (role == null) {
            return;
        }

        String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
        // KEYCLOAK-8730 if a previous mapper has already granted the same role, skip the checks so we don't accidentally remove a valid role.
        if (!context.hasMapperGrantedRole(roleName)) {
            if (this.applies(mapperModel, context)) {
                context.addMapperGrantedRole(roleName);
                user.grantRole(role);
            } else {
                user.deleteRoleMapping(role);
            }
        }
    }
