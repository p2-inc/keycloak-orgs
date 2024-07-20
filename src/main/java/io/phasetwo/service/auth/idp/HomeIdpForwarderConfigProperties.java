//package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;


import static io.phasetwo.service.auth.idp.HomeIdpForwarderConfig.BYPASS_LOGIN_PAGE;
import static io.phasetwo.service.auth.idp.HomeIdpForwarderConfig.FORWARD_TO_FIRST_MATCH;
import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
final class HomeIdpForwarderConfigProperties {
    private static final ProviderConfigProperty BYPASS_LOGIN_PAGE_PROPERTY = new ProviderConfigProperty(
            BYPASS_LOGIN_PAGE,
            "Bypass login page",
            "If OIDC login_hint parameter is present, whether to bypass the login page for managed domains or not.",
            BOOLEAN_TYPE,
            false,
            false);

    private static final ProviderConfigProperty FORWARD_TO_FIRST_MATCH_PROPERTY = new ProviderConfigProperty(
            FORWARD_TO_FIRST_MATCH,
            "Forward to first matched IdP",
            "When multiple IdPs match the domain, whether to forward to the first IdP found or let the user choose.",
            BOOLEAN_TYPE,
            true,
            false);

    static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
            .property(BYPASS_LOGIN_PAGE_PROPERTY)
            .property(FORWARD_TO_FIRST_MATCH_PROPERTY)
            .build();

}