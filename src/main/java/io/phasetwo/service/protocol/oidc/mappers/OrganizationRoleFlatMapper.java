package io.phasetwo.service.protocol.oidc.mappers;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import io.phasetwo.service.model.OrganizationProvider;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.ProtocolMapper;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

@JBossLog
@AutoService(ProtocolMapper.class)
public class OrganizationRoleFlatMapper extends AbstractOrganizationMapper {

    public static final String PROVIDER_ID = "oidc-organization-role-flattened-mapper";

    public static List<ProviderConfigProperty> configProperties = Lists.newArrayList();

    static {
        configProperties = ProviderConfigurationBuilder.create()
                .property()
                .name(ProtocolMapperUtils.MULTIVALUED)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .label(ProtocolMapperUtils.MULTIVALUED_LABEL)
                .helpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT)
                .defaultValue(true)
                .add()
                .build();
        OIDCAttributeMapperHelper.addAttributeConfig(configProperties, OrganizationRoleFlatMapper.class);
    }

    public OrganizationRoleFlatMapper() {
        super(
                PROVIDER_ID,
                "Organization Roles Flattened",
                TOKEN_MAPPER_CATEGORY,
                "Map organization roles in a token claim flattened.",
                configProperties);
    }

    /*
     * roles: [
     * {organizationName}/{roleName}
     * ]
     * gets all the roles for each organization of which the user is a member
     */
    @Override
    protected List<String> getOrganizationClaim(
            KeycloakSession session, RealmModel realm, UserModel user) {
        OrganizationProvider orgs = session.getProvider(OrganizationProvider.class);

        List<String> claim = Lists.newArrayList();

        orgs.getUserOrganizationsStream((realm), user)
                .forEach(o -> {
                    o.getRolesStream()
                            .forEach(r -> {
                                if (r.hasRole(user)) {
                                    claim.add(String.format("%s/%s", o.getName(), r.getName()));
                                }
                            });
                });

        log.debugf("created user %s claim %s", user.getUsername(), claim);
        return claim;
    }
}
