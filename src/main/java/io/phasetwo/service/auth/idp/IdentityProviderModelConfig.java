//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;

import java.util.Arrays;
import java.util.stream.Stream;

final class IdentityProviderModelConfig {

    private static final String DOMAINS_ATTRIBUTE_KEY = "home.idp.discovery.domains";

    private final IdentityProviderModel identityProviderModel;

    IdentityProviderModelConfig(IdentityProviderModel identityProviderModel) {
        this.identityProviderModel = identityProviderModel;
    }

    boolean hasDomain(String userAttributeName, String domain) {
        return getDomains(userAttributeName).anyMatch(domain::equalsIgnoreCase);
    }

    private Stream<String> getDomains(String userAttributeName) {
        String key = getDomainConfigKey(userAttributeName);
        String domainsAttribute = identityProviderModel.getConfig().getOrDefault(key, "");
        return Arrays.stream(Constants.CFG_DELIMITER_PATTERN.split(domainsAttribute));
    }

    private String getDomainConfigKey(String userAttributeName) {
        String key = DOMAINS_ATTRIBUTE_KEY;
        if (userAttributeName != null) {
            final String candidateKey = DOMAINS_ATTRIBUTE_KEY + "." + userAttributeName;
            if (identityProviderModel.getConfig().containsKey(candidateKey)) {
                key = candidateKey;
            }
        }
        return key;
    }

}
