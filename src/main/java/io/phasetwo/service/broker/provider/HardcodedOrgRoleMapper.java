package io.phasetwo.service.broker.provider;

import com.google.common.collect.MoreCollectors;
import com.google.common.base.Strings;
import com.google.auto.service.AutoService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.IdentityProviderMapper;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.IdentityProviderSyncMode;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.ProviderConfigProperty;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;

@JBossLog
@AutoService(IdentityProviderMapper.class)
public class HardcodedOrgRoleMapper extends AbstractIdentityProviderMapper {
  protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES =
      new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  static {
    ProviderConfigProperty org = new ProviderConfigProperty();
    org.setName("org");
    org.setLabel("Organization");
    org.setHelpText("Organization containing the role to grant to user.");
    org.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(org);
    ProviderConfigProperty orgRole = new ProviderConfigProperty();
    orgRole.setName("org_role");
    orgRole.setLabel("Organization Role");
    orgRole.setHelpText("Organization role to grant to user.");
    orgRole.setType(ProviderConfigProperty.STRING_TYPE);
    configProperties.add(orgRole);
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getDisplayCategory() {
    return "Role Importer";
  }

  @Override
  public String getDisplayType() {
    return "Hardcoded Organization Role";
  }

  public static final String[] COMPATIBLE_PROVIDERS = {ANY_PROVIDER};

  public static final String PROVIDER_ID = "oidc-hardcoded-org-role-idp-mapper";

  @Override
  public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
    return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
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
  public void importNewUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    grantOrgRole(session, realm, user, mapperModel);
  }

  private void grantOrgRole(KeycloakSession session,
                            RealmModel realm,
                            UserModel user,
                            IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().get("org");
    String orgRoleName = mapperModel.getConfig().get("org_role");
    if (Strings.isNullOrEmpty(orgName) || Strings.isNullOrEmpty(orgRoleName)) return;

    OrganizationModel> org = orgs.searchForOrganizationByNameStream(realm, orgName, 0, 1).filter(o -> o.getName().equals(orgName))
                             .collect(MoreCollectors.toOptional())
                             .orElse(null);
    if (org == null) {
      log.debugf("Cannot map non-existent org %s", orgName);
      return;
    }
    OrganizationRoleModel role = org.getRoleByName(orgRoleName);
    if (role == null) {
      log.debugf("Cannot map non-existent org role %s - %s", orgName, orgRoleName);
      return;
    }
    log.infof("Granting org: %s - role: %s to %s", orgName, orgRoleName, user.getUsername());
    role.grantRole(user);
  }

  @Override
  public void updateBrokeredUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    grantOrgRole(realm, user, mapperModel);
  }

  @Override
  public void updateBrokeredUserLegacy(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {}

  @Override
  public String getHelpText() {
    return "When user is imported from provider, hardcode an organization role mapping for it.";
  }
}
