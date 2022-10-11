// package de.sventorben.keycloak.authentication.hidpd;
package io.phasetwo.service.auth.idp;

import static org.keycloak.provider.ProviderConfigProperty.BOOLEAN_TYPE;
import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;

import java.util.List;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

final class HomeIdpDiscoveryConfigProperties {

  private static final ProviderConfigProperty FORWARD_TO_LINKED_IDP_PROPERTY =
      new ProviderConfigProperty(
          HomeIdpDiscoveryConfig.FORWARD_TO_LINKED_IDP,
          "Forward to linked IdP",
          "Whether to forward existing user to a linked identity provider or not.",
          BOOLEAN_TYPE,
          false,
          false);

  private static final ProviderConfigProperty REQUIRE_VERIFIED_EMAIL_PROPERTY =
      new ProviderConfigProperty(
          HomeIdpDiscoveryConfig.REQUIRE_VERIFIED_EMAIL,
          "Require a verified email",
          "Whether a verified email address for a user is required to forward to their identity provider.",
          BOOLEAN_TYPE,
          false,
          false);

  private static final ProviderConfigProperty REQUIRE_VERIFIED_DOMAIN_PROPERTY =
      new ProviderConfigProperty(
          HomeIdpDiscoveryConfig.REQUIRE_VERIFIED_DOMAIN,
          "Require a verified domain",
          "Whether a verified domain name for an organization is required to forward to their identity provider.",
          BOOLEAN_TYPE,
          false,
          false);

  private static final ProviderConfigProperty USER_ATTRIBUTE_PROPERTY =
      new ProviderConfigProperty(
          HomeIdpDiscoveryConfig.USER_ATTRIBUTE,
          "User attribute",
          "The user attribute used to lookup the email address of the user.",
          STRING_TYPE,
          "email",
          false);

  static final List<ProviderConfigProperty> CONFIG_PROPERTIES =
      ProviderConfigurationBuilder.create()
          .property(USER_ATTRIBUTE_PROPERTY)
          .property(REQUIRE_VERIFIED_EMAIL_PROPERTY)
          .property(REQUIRE_VERIFIED_DOMAIN_PROPERTY)
          .property(FORWARD_TO_LINKED_IDP_PROPERTY)
          .build();
}
