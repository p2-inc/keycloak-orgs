package io.phasetwo.service.broker;

import static io.phasetwo.service.broker.Mappers.*;

import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public interface OrgRoleMapper {

  static final Logger log = Logger.getLogger(OrgRoleMapper.class);

  static void addOrgConfigProperties(List<ProviderConfigProperty> configProperties) {
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

  default boolean orgAdd(IdentityProviderMapperModel mapperModel) {
    return Boolean.parseBoolean(
        mapperModel.getConfig().getOrDefault(ORG_ADD_PROPERTY_NAME, "false"));
  }

  default void addUserToOrg(
      OrganizationModel org, UserModel user, IdentityProviderMapperModel mapperModel) {
    if (orgAdd(mapperModel) && !org.hasMembership(user)) {
      org.grantMembership(user);
    }
  }

  default OrganizationModel getOrg(
      KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().getOrDefault(ORG_PROPERTY_NAME, null);
    if (Strings.isNullOrEmpty(orgName)) return null;
    return orgs.getOrganizationByName(realm, orgName);
  }

  default OrganizationRoleModel getOrgRole(
      KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().getOrDefault(ORG_PROPERTY_NAME, null);
    String orgRoleName = mapperModel.getConfig().getOrDefault(ORG_ROLE_PROPERTY_NAME, null);
    if (Strings.isNullOrEmpty(orgName) || Strings.isNullOrEmpty(orgRoleName)) return null;
    else return getOrgRole(orgs, orgName, orgRoleName, realm);
  }

  default OrganizationRoleModel getOrgRole(
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

  default void grantOrgRole(OrganizationModel org, OrganizationRoleModel role, UserModel user) {
    if (org.hasMembership(user)) {
      role.grantRole(user);
    }
  }

  default void revokeOrgRole(OrganizationModel org, OrganizationRoleModel role, UserModel user) {
    if (org.hasMembership(user)) {
      role.revokeRole(user);
    }
  }

  default void grantOrgRole(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().getOrDefault(ORG_PROPERTY_NAME, null);
    String orgRoleName = mapperModel.getConfig().getOrDefault(ORG_ROLE_PROPERTY_NAME, null);
    if (Strings.isNullOrEmpty(orgName) || Strings.isNullOrEmpty(orgRoleName)) return;

    OrganizationModel org = orgs.getOrganizationByName(realm, orgName);
    OrganizationRoleModel role = getOrgRole(orgs, orgName, orgRoleName, realm);
    if (org != null && role != null) {
      if (orgAdd(mapperModel)) {
        if (!org.hasMembership(user)) {
          log.infof("Granting org: %s membership to %s", orgName, user.getUsername());
          org.grantMembership(user);
        }
      }
      if (org.hasMembership(user)) {
        log.infof("Granting org: %s - role: %s to %s", orgName, orgRoleName, user.getUsername());
        role.grantRole(user);
      }
    }
  }
}
