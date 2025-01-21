package io.phasetwo.service.protocol.saml.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.saml.mappers.AbstractSAMLProtocolMapper;
import org.keycloak.protocol.saml.mappers.AttributeStatementHelper;
import org.keycloak.protocol.saml.mappers.SAMLAttributeStatementMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationRoleMapper extends AbstractSAMLProtocolMapper
    implements SAMLAttributeStatementMapper {

  public static final String ID = "saml-ext-organization-role-mapper";
  public static final String ORGANIZATION_ATTRIBUTE_NAME = "organization";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayType() {
    return "Organization Role";
  }

  @Override
  public String getDisplayCategory() {
    return AttributeStatementHelper.ATTRIBUTE_STATEMENT_CATEGORY;
  }

  @Override
  public String getHelpText() {
    return "Map an attribute to the assertion with information with organization membership roles.";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ImmutableList.of();
  }

  @Override
  public void transformAttributeStatement(
      AttributeStatementType attributeStatement,
      ProtocolMapperModel mappingModel,
      KeycloakSession session,
      UserSessionModel userSession,
      AuthenticatedClientSessionModel clientSession) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);

    RealmModel realm = userSession.getRealm();
    UserModel user = userSession.getUser();
    AttributeType attribute = new AttributeType(ORGANIZATION_ATTRIBUTE_NAME);
    attribute.setFriendlyName(ORGANIZATION_ATTRIBUTE_NAME);
    attribute.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_BASIC.get());

    orgs.getUserOrganizationsStream(realm, user)
        .forEach(
            o -> {
              o.getRolesStream()
                  .forEach(
                      r -> {
                        if (r.hasRole(user)) {
                          String orgRole = String.format("%s/%s", o.getId(), r.getName());
                          log.debugf("added attributeValue %s", orgRole);
                          attribute.addAttributeValue(orgRole);
                        }
                      });
            });

    if (attribute.getAttributeValue().isEmpty()) {
      return;
    }

    attributeStatement.addAttribute(new AttributeStatementType.ASTChoiceType(attribute));
  }
}
