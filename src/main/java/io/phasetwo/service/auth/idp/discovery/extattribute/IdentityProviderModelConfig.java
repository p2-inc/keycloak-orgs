package io.phasetwo.service.auth.idp.discovery.extattribute;

import io.phasetwo.service.auth.idp.discovery.extemail.Domain;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;

import java.util.Arrays;

final class IdentityProviderModelConfig {

    private static final String USER_ATTRIBUTE_KEY = "home.idp.discovery.user-attributes";

    private final IdentityProviderModel identityProviderModel;

    IdentityProviderModelConfig(IdentityProviderModel identityProviderModel) {
        this.identityProviderModel = identityProviderModel;
    }

    boolean supportsAttribute(String att) {
        String userAttributeValue = getUserAttributeConfigKey();
        return Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(userAttributeValue)).contains(att);
    }

    private String getUserAttributeConfigKey() {
        if (identityProviderModel.getConfig().containsKey(USER_ATTRIBUTE_KEY)) {
           return identityProviderModel.getConfig().get(USER_ATTRIBUTE_KEY);
        }

        return "";
    }
}
