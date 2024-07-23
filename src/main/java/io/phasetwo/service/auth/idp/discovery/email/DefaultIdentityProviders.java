//package io.phasetwo.service.auth.idp.discovery.email;
package io.phasetwo.service.auth.idp.discovery.email;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;

import java.util.List;
import java.util.stream.Collectors;

final class DefaultIdentityProviders implements IdentityProviders {

    private static final Logger LOG = Logger.getLogger(DefaultIdentityProviders.class);

    @Override
    public List<IdentityProviderModel> withMatchingDomain(AuthenticationFlowContext context, List<IdentityProviderModel> candidates, Domain domain) {
        EmailHomeIdpDiscovererConfig config = new EmailHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
        String userAttributeName = config.userAttribute();
        List<IdentityProviderModel> idpsWithMatchingDomain = candidates.stream()
            .filter(it -> new IdentityProviderModelConfig(it).supportsDomain(userAttributeName, domain))
            .collect(Collectors.toList());
        LOG.tracef("IdPs with matching domain '%s' for attribute '%s': %s", domain, userAttributeName,
            idpsWithMatchingDomain.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return idpsWithMatchingDomain;
    }

}
