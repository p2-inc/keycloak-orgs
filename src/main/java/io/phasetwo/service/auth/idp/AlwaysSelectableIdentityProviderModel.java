//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.models.IdentityProviderModel;

import java.util.HashMap;
import java.util.Map;

final class AlwaysSelectableIdentityProviderModel extends IdentityProviderModel {

    AlwaysSelectableIdentityProviderModel(IdentityProviderModel delegate) {
        super(delegate);
    }

    @Override
    public boolean isHideOnLogin() {
        return false;
    }

    @Override
    public Map<String, String> getConfig() {
        Map<String, String> superConfig = new HashMap<>(super.getConfig());
        superConfig.put("hideOnLoginPage", Boolean.FALSE.toString());
        return superConfig;
    }

}
