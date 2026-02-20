package io.phasetwo.service.auth.idp.discovery.extattribute;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;

import java.util.List;
import java.util.stream.Collectors;

final class DefaultIdentityProviders implements IdentityProviders {

    @Override
    public List<IdentityProviderModel> withMatchingAttribute(AuthenticationFlowContext context, List<IdentityProviderModel> candidates, String attribute) {
        UserAttributeHomeIdpDiscovererConfig config = new UserAttributeHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
        String userAttributeName = config.userAttribute();

        List<IdentityProviderModel> idpsWithMatchingDomain = candidates.stream()
                .filter(it -> new IdentityProviderModelConfig(it).supportsAttribute(attribute))
                .collect(Collectors.toList());
        LOG.tracef("IdPs with matching attribute '%s' for attribute '%s': %s", userAttributeName, attribute,
                idpsWithMatchingDomain.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return idpsWithMatchingDomain;
    }
}