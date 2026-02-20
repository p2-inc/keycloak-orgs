//package de.sventorben.keycloak.authentication.hidpd.discovery.spi;
package io.phasetwo.service.auth.idp.discovery.spi;

import io.phasetwo.service.auth.idp.PublicAPI;
import org.keycloak.provider.ProviderFactory;


@PublicAPI(unstable = true)
public interface HomeIdpDiscovererFactory extends ProviderFactory<HomeIdpDiscoverer> {
}
