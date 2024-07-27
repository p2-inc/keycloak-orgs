package de.sventorben.keycloak.authentication.hidpd;

import de.sventorben.keycloak.authentication.hidpd.discovery.email.Domain;
import de.sventorben.keycloak.authentication.hidpd.discovery.email.IdentityProviders;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;

import java.util.List;
import java.util.stream.Collectors;

final class OrgsIdentityProviders implements IdentityProviders {

    @Override
    public List<IdentityProviderModel> withMatchingDomain(AuthenticationFlowContext context, List<IdentityProviderModel> candidates, Domain domain) {
        var orgs = context.getSession().getProvider(OrganizationProvider.class);
        var config = new OrgsEmailHomeIdpDiscovererConfig(context.getAuthenticatorConfig());
        return orgs.getOrganizationsStreamForDomain(
                        context.getRealm(), domain.toString(), config.requireVerifiedDomain())
                .flatMap(OrganizationModel::getIdentityProvidersStream)
                .filter(IdentityProviderModel::isEnabled)
                .collect(Collectors.toList());
    }
}
