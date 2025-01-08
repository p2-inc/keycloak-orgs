package io.phasetwo.service.broker.oidc.mappers;

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

@JBossLog
@AutoService(IdentityProviderMapper.class)
public class AdvancedClaimToOrgRoleMapper extends AbstractClaimMapper {

  public static final String[] COMPATIBLE_PROVIDERS = {
    KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID
  };

  private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    ProviderConfigProperty claimsProperty = new ProviderConfigProperty();
    claimsProperty.setName(CLAIM_PROPERTY_NAME);
    claimsProperty.setLabel("Claims");
    claimsProperty.setHelpText(
        "Name and value of the claims to search for in token. You can reference nested claims using a '.', i.e. 'address.locality'. To use dot (.) literally, escape it with backslash (\\.)");
    claimsProperty.setType(ProviderConfigProperty.MAP_TYPE);
    configProperties.add(claimsProperty);
    ProviderConfigProperty isClaimValueRegexProperty = new ProviderConfigProperty();
    isClaimValueRegexProperty.setName(ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME);
    isClaimValueRegexProperty.setLabel("Regex Claim Values");
    isClaimValueRegexProperty.setHelpText(
        "If enabled claim values are interpreted as regular expressions.");
    isClaimValueRegexProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
    configProperties.add(isClaimValueRegexProperty);

    addOrgConfigProperties(configProperties);
  }

  public static final String PROVIDER_ID = "oidc-advanced-org-role-idp-mapper";

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
    return "Advanced Claim to Org Role";
  }

  @Override
  public String getHelpText() {
    return "If all claims exists, grant the user the specified organization role.";
  }

  protected boolean applies(
      IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
    Map<String, List<String>> claims = mapperModel.getConfigMap(CLAIM_PROPERTY_NAME);
    boolean areClaimValuesRegex =
        Boolean.parseBoolean(mapperModel.getConfig().get(ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME));

    for (Map.Entry<String, List<String>> claim : claims.entrySet()) {
      Object claimValue = getClaimValue(context, claim.getKey());
      for (String value : claim.getValue()) {
        boolean claimValuesMismatch =
            !(areClaimValuesRegex
                ? valueMatchesRegex(value, claimValue)
                : valueEquals(value, claimValue));
        if (claimValuesMismatch) {
          return false;
        }
      }
    }

    return true;
  }

  private boolean orgAdd(IdentityProviderMapperModel mapperModel) {
    return Boolean.parseBoolean(mapperModel.getConfig().get(ORG_ADD_PROPERTY_NAME));
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
  public void updateBrokeredUserLegacy(
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

    if (!applies(mapperModel, context)) {
      revokeOrgRole(org, role, user);
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

  private void addUserToOrg(
      OrganizationModel org, UserModel user, IdentityProviderMapperModel mapperModel) {
    if (orgAdd(mapperModel) && !org.hasMembership(user)) {
      org.grantMembership(user);
    }
  }

  private OrganizationModel getOrg(
      KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().get(ORG_PROPERTY_NAME);
    return orgs.getOrganizationByName(realm, orgName);
  }

  private OrganizationRoleModel getOrgRole(
      KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().get(ORG_PROPERTY_NAME);
    String orgRoleName = mapperModel.getConfig().get(ORG_ROLE_PROPERTY_NAME);
    if (Strings.isNullOrEmpty(orgName) || Strings.isNullOrEmpty(orgRoleName)) return null;
    else return getOrganizationRole(orgs, orgName, orgRoleName, realm);
  }

  private void grantOrgRole(OrganizationModel org, OrganizationRoleModel role, UserModel user) {
    if (org.hasMembership(user)) {
      log.infof("Granting role: %s to %s", role.getName(), user.getUsername());
      role.grantRole(user);
    }
  }

  private void revokeOrgRole(OrganizationModel org, OrganizationRoleModel role, UserModel user) {
    if (org.hasMembership(user)) {
      log.infof("Revoking role: %s to %s", role.getName(), user.getUsername());
      role.revokeRole(user);
    }
  }
}
