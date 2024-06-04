package io.phasetwo.service.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;
import org.keycloak.models.RoleModel;

public interface TierMapperModel {
  void addTier(RoleModel role, LocalDate expireDate);

  void removeTier(RoleModel role);

  default boolean hasTier(RoleModel role) {
    return getTierMappingsStream().anyMatch(t -> Objects.equals(t.getRoleId(), role.getId()));
  }

  Stream<TierModel> getRealmTierMappingsStream();

  Stream<TierModel> getTierMappingsStream();
}
