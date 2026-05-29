package io.phasetwo.service.auth.idp.discovery.extattribute;

import static io.phasetwo.service.auth.idp.discovery.extattribute.IdentityProviders.USER_ATTRIBUTE_KEY;

import java.util.Arrays;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;

final class IdentityProviderModelConfig {

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
