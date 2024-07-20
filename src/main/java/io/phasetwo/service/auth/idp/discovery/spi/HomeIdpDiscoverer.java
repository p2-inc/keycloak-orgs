//package io.phasetwo.service.auth.idp.discovery.spi;
package io.phasetwo.service.auth.idp.discovery.spi;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.provider.Provider;
import io.phasetwo.service.auth.idp.PublicAPI;

import java.util.List;

/**
 * The {@link HomeIdpDiscoverer} defines the contract for implementations
 * responsible for discovering the Home Identity Provider(s) for a given user.
 * This interface is a part of the Service Provider Interface (SPI) extension for the
 * Home IdP Discovery Keycloak extension, aimed at enabling dynamic discovery of a user's
 * Home IdP based on custom logic and criteria.
 * <p>
 * Implementations of this interface should provide the logic to determine the appropriate
 * Home IdP(s) for a user, potentially based on attributes such as the username, domain, or
 * other identifiers associated with the user's account. This is particularly useful in
 * scenarios where users may belong to different IdPs based on their organization, domain,
 * or other factors, and an automated method is required to direct the user to their
 * respective IdP for authentication.
 * </p>
 *
 * @apiNote This interface is part of the public API, but is currently unstable and may change in future releases.
 *
 * @see IdentityProviderModel
 * @see AuthenticationFlowContext
 */
@PublicAPI(unstable = true)
public interface HomeIdpDiscoverer extends Provider {

    /**
     * Discovers and returns a list of {@link IdentityProviderModel} instances representing
     * the Home Identity Provider(s) for the specified user. The method takes the username
     * of the user as a parameter and returns a list of IdP models that are considered the
     * user's home IdPs. If no home IdP is found for the user, this method may return an
     * empty list.
     * <p>
     * Implementors should ensure that the logic for discovering the home IdPs is efficient
     * and accounts for various criteria that may determine the user's Home IdP(s). The
     * criteria and the discovery logic are dependent on the specific implementation.
     * </p>
     * @param context the {@link AuthenticationFlowContext} providing the current state and parameters
     *                of the authentication flow. This context can include various details such as the
     *                client, session, and other relevant information that can be utilized to determine
     *                the most appropriate home IdP(s) for the user. Implementors can use this context
     *                to access additional attributes or perform more complex logic based on the current
     *                authentication flow.
     * @param username the unvalidated username provided by the user, serving as the primary identifier
     *                 for the discovery of the user's Home IdP(s). Given that this username is unvalidated
     *                 input, implementors should apply appropriate validation or sanitization measures
     *                 to mitigate potential security risks or logic errors. This consideration is
     *                 crucial, especially in scenarios where multiple users across different realms or
     *                 IdPs might share the same username, necessitating the use of the authentication
     *                 flow context to resolve such ambiguities.
     * @return a list of {@link IdentityProviderModel} instances representing the discovered
     *         home IdP(s) for the user. The list may be empty if no home IdP is associated
     *         with the user. Do not return {@code null}.
     */
    List<IdentityProviderModel> discoverForUser(AuthenticationFlowContext context, String username);
}
