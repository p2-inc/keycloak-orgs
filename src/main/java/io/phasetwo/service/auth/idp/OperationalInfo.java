//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import java.util.Map;

public final class OperationalInfo {

    public static Map<String, String> get() {
        String version = OperationalInfo.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "dev-snapshot";
        }
        return Map.of("Version", version);
    }

}
