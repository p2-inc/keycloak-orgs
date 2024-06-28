package io.phasetwo.service.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.keycloak.models.Constants;

public final class IdentityProviders {

  private IdentityProviders() {}

  public static Set<String> getAttributeMultivalued(Map<String, String> config, String attrKey) {
    if (config == null) {
      return new HashSet<>();
    }
    String attrValue = config.get(attrKey);
    if (attrValue == null) return new HashSet<>();
    return new HashSet<>(Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(attrValue)));
  }

  public static void setAttributeMultivalued(
      Map<String, String> config, String attrKey, Set<String> attrValues) {
    if (config == null) {
      return;
    }

    if (attrValues == null || attrValues.size() == 0) {
      // Remove attribute
      config.put(attrKey, null);
    } else {
      String attrValueFull = String.join(Constants.CFG_DELIMITER, attrValues);
      config.put(attrKey, attrValueFull);
    }
  }
}
