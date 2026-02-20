//package de.sventorben.keycloak.authentication.hidpd.discovery.email;
package io.phasetwo.service.auth.idp.discovery.extemail;

import io.phasetwo.service.auth.idp.PublicAPI;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the contract for filtering and retrieving identity providers based on domain-specific
 * criteria within the authentication process. This interface allows for the dynamic
 * selection of identity providers (IdPs) that match certain conditions, enhancing the flexibility
 * and precision of home IdP discovery mechanisms.
 * <p>
 * Implementations of this interface should provide logic to filter identity providers based on
 * custom criteria such as the domain associated with the user or other relevant factors.
 * </p>
 *
 * to changes in future releases.
 */
@PublicAPI(unstable = true)
public interface IdentityProviders {

    Logger LOG = Logger.getLogger(IdentityProviders.class);

    /**
     * Filters the given list of identity provider candidates to return those that match a specified
     * domain within the context of an authentication flow.
     *
     * @param context The authentication flow context providing runtime information about the
     *                current authentication process.
     * @param candidates A list of potentially eligible identity providers that may be suitable
     *                   for the user based on initial criteria (see {@code #candidatesForHomeIdp}).
     * @param domain The domain criteria used to match identity providers.
     * @return A filtered list of {@link IdentityProviderModel} that match the specified domain criteria.
     * May be empty but not {@code null}.
     */
    List<IdentityProviderModel> withMatchingDomain(AuthenticationFlowContext context, List<IdentityProviderModel> candidates, Domain domain);

    /**
     * Retrieves a list of identity providers that are candidates for being the user's home IdP.
     * <p>
     * This default method filters out and collects only those providers that are enabled.
     *</p>
     * @param context The authentication flow context providing runtime information about the
     *                current authentication process.
     * @return A list of {@link IdentityProviderModel} from the realm. May be empty but not {@code null}.
     */
    default List<IdentityProviderModel> candidatesForHomeIdp(AuthenticationFlowContext context, UserModel user) {
        RealmModel realm = context.getRealm();
        List<IdentityProviderModel> enabledIdps = realm.getIdentityProvidersStream()
            .filter(IdentityProviderModel::isEnabled)
            .collect(Collectors.toList());
        LOG.tracef("Enabled IdPs in realm '%s': %s",
            realm.getName(),
            enabledIdps.stream().map(IdentityProviderModel::getAlias).collect(Collectors.joining(",")));
        return enabledIdps;
    }

}
