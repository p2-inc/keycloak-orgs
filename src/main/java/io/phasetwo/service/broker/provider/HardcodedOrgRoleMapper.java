package io.phasetwo.service.broker.provider;

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

@JBossLog
@AutoService(IdentityProviderMapper.class)
public class HardcodedOrgRoleMapper extends AbstractIdentityProviderMapper {
  protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES =
      new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

  static {
    ProviderConfigProperty property;
    property = new ProviderConfigProperty();
    property.setName(ConfigConstants.ROLE);
    property.setLabel("Role");
    property.setHelpText(
        "Role to grant to user.  Click 'Select Role' button to browse roles, or just type it in the textbox.  To reference a client role the syntax is clientname.clientrole, i.e. myclient.myrole");
    property.setType(ProviderConfigProperty.ROLE_TYPE);
    configProperties.add(property);
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
    return "Hardcoded Role";
  }

  public static final String[] COMPATIBLE_PROVIDERS = {ANY_PROVIDER};

  public static final String PROVIDER_ID = "oidc-hardcoded-role-idp-mapper";

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
    grantUserRole(realm, user, mapperModel);
  }

  private void grantUserRole(
      RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel) {
    RoleModel role = getRole(realm, mapperModel);
    if (role != null) {
      user.grantRole(role);
    }
  }

  private RoleModel getRole(final RealmModel realm, final IdentityProviderMapperModel mapperModel) {
    String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
    RoleModel role = KeycloakModelUtils.getRoleFromString(realm, roleName);

    if (role == null) {
      log.warnf(
          "Unable to find role '%s' referenced by mapper '%s' on realm '%s'.",
          roleName, mapperModel.getName(), realm.getName());
    }

    return role;
  }

  @Override
  public void updateBrokeredUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    grantUserRole(realm, user, mapperModel);
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
    return "When user is imported from provider, hardcode a role mapping for it.";
  }
}
