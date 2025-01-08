package io.phasetwo.service.broker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.IdentityProviderSyncMode;

@JBossLog
public final class Mappers {

  public static final String ARE_ATTRIBUTE_VALUES_REGEX_PROPERTY_NAME =
      "are.attribute.values.regex";
  public static final String ARE_CLAIM_VALUES_REGEX_PROPERTY_NAME = "are.claim.values.regex";
  public static final String ATTRIBUTE_PROPERTY_NAME = "attributes";
  public static final String CLAIM_PROPERTY_NAME = "claims";
  public static final String ORG_ADD_PROPERTY_NAME = "org_add";
  public static final String ORG_PROPERTY_NAME = "org";
  public static final String ORG_ROLE_PROPERTY_NAME = "org_role";

  public static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES =
      new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));
}
