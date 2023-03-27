package io.phasetwo.service.protocol.oidc.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationRoleMapper extends AbstractOrganizationMapper {

  public static final String PROVIDER_ID = "oidc-organization-role-mapper";

  private static final List<ProviderConfigProperty> configProperties = Lists.newArrayList();

  static {
    OIDCAttributeMapperHelper.addAttributeConfig(configProperties, OrganizationRoleMapper.class);
  }

  public OrganizationRoleMapper() {
    super(
        PROVIDER_ID,
        "Organization Role",
        TOKEN_MAPPER_CATEGORY,
        "Map organization roles in a token claim.",
        configProperties);
  }

  /*
   organizations: [
     foo: [
       "admin"
          ],
     bar: []
   ]
   gets all the roles for each organization of which the user is a member
  */
  @Override
  protected Map<String, Object> getOrganizationClaim(
      KeycloakSession session, RealmModel realm, UserModel user) {
    OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);
    Map<String, Object> claim = Maps.newHashMap();
    orgs.getUserOrganizationsStream(realm, user)
        .forEach(
            org -> {
              Set<String> roles = Sets.newLinkedHashSet();
              org.getRolesStream()
                  .filter(r -> r.hasRole(user))
                  .forEach(r -> roles.add(r.getName()));

              Map<String, Object> organization = Maps.newHashMap();
              organization.put("name", org.getName());
              organization.put("roles", roles);
              claim.put(org.getId(), organization);
            });
    log.debugf("created user %s claim %s", user.getUsername(), claim);
    return claim;
  }
}
