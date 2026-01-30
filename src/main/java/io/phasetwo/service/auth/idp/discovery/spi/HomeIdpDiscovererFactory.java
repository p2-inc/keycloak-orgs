//package de.sventorben.keycloak.authentication.hidpd.discovery.spi;
package io.phasetwo.service.auth.idp.discovery.spi;

import io.phasetwo.service.auth.idp.PublicAPI;
import org.keycloak.provider.ProviderFactory;

/**
 * @apiNote This interface is part of the public API, but is currently unstable and may change in future releases.
 */
@PublicAPI(unstable = true)
public interface HomeIdpDiscovererFactory extends ProviderFactory<HomeIdpDiscoverer> {
}
