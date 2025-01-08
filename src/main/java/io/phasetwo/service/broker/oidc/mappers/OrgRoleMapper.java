package io.phasetwo.service.broker;

import static io.phasetwo.service.broker.Mappers.getOrganizationRole;
import static org.keycloak.utils.RegexUtils.valueMatchesRegex;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.oidc.KeycloakOIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.OIDCIdentityProviderFactory;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

public interface OrgRoleMapper {
  
  default void addUserToOrg(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().get("org");
    OrganizationModel org = orgs.getOrganizationByName(realm, orgName);
    org.grantMembership(user);
  }

  default OrganizationRoleModel getOrgRole(
      KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().get("org");
    String orgRoleName = mapperModel.getConfig().get("org_role");
    if (Strings.isNullOrEmpty(orgName) || Strings.isNullOrEmpty(orgRoleName)) return null;
    else return getOrganizationRole(orgs, orgName, orgRoleName, realm);
  }

  default void grantOrgRole(OrganizationRoleModel role, UserModel user) {
    role.grantRole(user);
  }

  default void revokeOrgRole(OrganizationRoleModel role, UserModel user) {
    role.revokeRole(user);
  }
  
}
