package io.phasetwo.service.broker;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.keycloak.models.IdentityProviderSyncMode;

@JBossLog
public class Mappers {

  public static final String ARE_ATTRIBUTE_VALUES_REGEX_PROPERTY_NAME = "are.attribute.values.regex";
  public static final String ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME = "are.claim.values.regex";
  public static final String ATTRIBUTE_PROPERTY_NAME = "attributes";
  public static final String CLAIM_PROPERTY_NAME = "claims";
  public static final String ORG_ADD_PROPERTY_NAME = "org_add";
  public static final String ORG_PROPERTY_NAME = "org";
  public static final String ORG_ROLE_PROPERTY_NAME = "org_role";

  public static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES =
      new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  public static addOrgConfigProperties(List<ProviderConfigProperty> configProperties) {
    ProviderConfigProperty orgAdd = new ProviderConfigProperty();
    orgAdd.setName(ORG_ADD_PROPERTY_NAME);
    orgAdd.setLabel("Add To Organization");
    orgAdd.setHelpText("Add user to the organization as a member if not already.");
    orgAdd.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    configProperties.add(orgAdd);
    ProviderConfigProperty org = new ProviderConfigProperty();
    org.setName(ORG_PROPERTY_NAME);
    org.setLabel("Organization");
    org.setHelpText("Organization containing the role to grant to user.");
    org.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(org);
    ProviderConfigProperty orgRole = new ProviderConfigProperty();
    orgRole.setName(ORG_ROLE_PROPERTY_NAME);
    orgRole.setLabel("Organization Role");
    orgRole.setHelpText("Organization role to grant to user.");
    orgRole.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(orgRole);
  }
  
  public static OrganizationRoleModel getOrganizationRole(
      OrganizationProvider orgs, String orgName, String orgRoleName, RealmModel realm) {
    OrganizationModel org = orgs.getOrganizationByName(realm, orgName);
    if (org == null) {
      log.debugf("Cannot map non-existent org %s", orgName);
      return null;
    }
    OrganizationRoleModel role = org.getRoleByName(orgRoleName);
    if (role == null) {
      log.debugf("Cannot map non-existent org role %s - %s", orgName, orgRoleName);
      return null;
    }
    return role;
  }
}
