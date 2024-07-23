//package io.phasetwo.service.auth.idp.discovery.orgs.domainhint;
package io.phasetwo.service.auth.idp.discovery.orgs.domainhint;

import io.phasetwo.service.auth.idp.discovery.spi.HomeIdpDiscoverer;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OrganizationModel;
import org.keycloak.organization.OrganizationProvider;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class OrgsDomainDiscoverer implements HomeIdpDiscoverer {

    private final KeycloakSession keycloakSession;

    OrgsDomainDiscoverer(KeycloakSession keycloakSession) {
        this.keycloakSession = keycloakSession;
    }

    @Override
    public List<IdentityProviderModel> discoverForUser(AuthenticationFlowContext context, String username) {
        String domain = username;
        OrganizationProvider orgProvider = keycloakSession.getProvider(OrganizationProvider.class);

        if (!orgProvider.isEnabled()) {
            return Collections.emptyList();
        }

        OrganizationModel org = orgProvider.getByDomainName(domain);
        if (org != null) {
            return org.getIdentityProviders()
                .filter(IdentityProviderModel::isEnabled)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void close() {

    }
}
