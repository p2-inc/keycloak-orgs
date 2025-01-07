package io.phasetwo.service.broker.oidc.mappers;

import com.google.auto.service.AutoService;
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
import org.keycloak.broker.provider.ConfigConstants;
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
public class AdvancedClaimToOrgRoleMapper extends AbstractClaimMapper {

  public static final String CLAIM_PROPERTY_NAME = "claims";
  public static final String ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME = "are.claim.values.regex";
  public static final String ORG_PROPERTY_NAME = "org";
  public static final String ORG_ROLE_PROPERTY_NAME = "org_role";
  
  public static final String[] COMPATIBLE_PROVIDERS = {
    KeycloakOIDCIdentityProviderFactory.PROVIDER_ID, OIDCIdentityProviderFactory.PROVIDER_ID
  };
  private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES =
      new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

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
    return "Advanced Claim to Role";
  }

  @Override
  public String getHelpText() {
    return "If all claims exists, grant the user the specified realm or client role.";
  }

  @Override
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

  @Override
  public void importNewUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    RoleModel role = getRole(realm, mapperModel);
    if (role == null) {
      return;
    }

    if (applies(mapperModel, context)) {
      user.grantRole(role);
    }
  }

  @Override
  public void updateBrokeredUserLegacy(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    RoleModel role = getRole(realm, mapperModel);
    if (role == null) {
      return;
    }

    if (!applies(mapperModel, context)) {
      user.deleteRoleMapping(role);
    }
  }

  @Override
  public void updateBrokeredUser(
      KeycloakSession session,
      RealmModel realm,
      UserModel user,
      IdentityProviderMapperModel mapperModel,
      BrokeredIdentityContext context) {
    RoleModel role = getRole(realm, mapperModel);
    if (role == null) {
      return;
    }

    String roleName = mapperModel.getConfig().get(ConfigConstants.ROLE);
    // KEYCLOAK-8730 if a previous mapper has already granted the same role, skip the checks so we
    // don't accidentally remove a valid role.
    if (!context.hasMapperGrantedRole(roleName)) {
      if (applies(mapperModel, context)) {
        context.addMapperGrantedRole(roleName);
        user.grantRole(role);
      } else {
        user.deleteRoleMapping(role);
      }
    }
  }

  /**
   * Obtains the {@link RoleModel} corresponding the role configured in the specified {@link
   * IdentityProviderMapperModel}. If the role doesn't correspond to one of the realm's client roles
   * or to one of the realm's roles, this method returns {@code null}.
   *
   * @param realm a reference to the realm.
   * @param mapperModel a reference to the {@link IdentityProviderMapperModel} containing the
   *     configured role.
   * @return the {@link RoleModel} that corresponds to the mapper model role; {@code null}, when
   *     role was not found
   */
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
}
