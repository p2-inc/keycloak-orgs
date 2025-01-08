package io.phasetwo.service.broker.provider;

import static io.phasetwo.service.broker.Mappers.*;

import com.google.auto.service.AutoService;
import io.phasetwo.service.broker.OrgRoleMapper;
import java.util.ArrayList;
import java.util.List;
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
public class HardcodedOrgRoleMapper extends AbstractIdentityProviderMapper
    implements OrgRoleMapper {
  protected static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

  static {
    OrgRoleMapper.addOrgConfigProperties(configProperties);
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
