package io.phasetwo.service.util;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static io.phasetwo.service.Orgs.ORG_SHARED_IDP_KEY;

import io.phasetwo.service.model.OrganizationModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

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

  public static void removeOrganization(String orgId, IdentityProviderModel idp) {
    var orgs = IdentityProviders.getAttributeMultivalued(idp.getConfig(), ORG_OWNER_CONFIG_KEY);
    orgs.remove(orgId);
    IdentityProviders.setAttributeMultivalued(idp.getConfig(), ORG_OWNER_CONFIG_KEY, orgs);
    if (orgs.size() > 1) {
      idp.getConfig().put(ORG_SHARED_IDP_KEY, "true");
    } else {
      idp.getConfig().put(ORG_SHARED_IDP_KEY, "false");
    }
  }

  public static void addMultiOrganization(
      OrganizationModel organization, IdentityProviderRepresentation representation) {
    var orgs =
        IdentityProviders.getAttributeMultivalued(representation.getConfig(), ORG_OWNER_CONFIG_KEY);
    orgs.add(organization.getId());
    IdentityProviders.setAttributeMultivalued(
        representation.getConfig(), ORG_OWNER_CONFIG_KEY, orgs);
    if (orgs.size() > 1) {
      representation.getConfig().put(ORG_SHARED_IDP_KEY, "true");
    } else {
      representation.getConfig().put(ORG_SHARED_IDP_KEY, "false");
    }
  }
}
