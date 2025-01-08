package io.phasetwo.service.broker.provider;

import static io.phasetwo.service.broker.Mappers.*;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.model.OrganizationRoleModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
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
public class HardcodedOrgRoleMapper extends AbstractIdentityProviderMapper {
  protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    addOrgConfigProperties(configProperties);
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

  private void grantOrgRole(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    String orgName = mapperModel.getConfig().get(ORG_PROPERTY_NAME);
    String orgRoleName = mapperModel.getConfig().get(ORG_ROLE_PROPERTY_NAME);
    boolean orgAdd = Boolean.parseBoolean(mapperModel.getConfig().get(ORG_ADD_PROPERTY_NAME));

    if (Strings.isNullOrEmpty(orgName) || Strings.isNullOrEmpty(orgRoleName)) return;

    OrganizationModel org = orgs.getOrganizationByName(realm, orgName);
    OrganizationRoleModel role = getOrganizationRole(orgs, orgName, orgRoleName, realm);
    if (org != null && role != null) {
      if (orgAdd) {
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

  @Override
  public void updateBrokeredUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    grantOrgRole(session, realm, user, mapperModel);
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
