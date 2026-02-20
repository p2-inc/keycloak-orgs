//package de.sventorben.keycloak.authentication.hidpd.discovery.spi;
package io.phasetwo.service.auth.idp.discovery.spi;

import com.google.auto.service.AutoService;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

@AutoService(Spi.class)
public final class HomeIdpDiscoverySpi implements Spi {

    private static final String SPI_NAME = "ext-hidpd-discovery";

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return SPI_NAME;
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return HomeIdpDiscoverer.class;
    }

    @Override
    public Class<? extends ProviderFactory<HomeIdpDiscoverer>> getProviderFactoryClass() {
        return HomeIdpDiscovererFactory.class;
    }
}
