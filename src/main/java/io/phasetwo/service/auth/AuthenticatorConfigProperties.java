package io.phasetwo.service.auth;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.keycloak.provider.ProviderConfigProperty;

public interface AuthenticatorConfigProperties {
  default List<ProviderConfigProperty> getConfigProperties() {
    return ImmutableList.of();
  }
}
