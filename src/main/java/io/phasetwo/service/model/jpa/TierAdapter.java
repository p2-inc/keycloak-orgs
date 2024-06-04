package io.phasetwo.service.model.jpa;

import io.phasetwo.service.model.TierModel;
import io.phasetwo.service.model.jpa.entity.OrganizationTierMappingEntity;
import java.time.LocalDate;
import org.keycloak.models.RoleModel;

public class TierAdapter implements TierModel {

  protected final OrganizationTierMappingEntity tier;
  protected final RoleModel role;

  public TierAdapter(RoleModel role, OrganizationTierMappingEntity tier) {
    this.role = role;
    this.tier = tier;
  }

  @Override
  public String getId() {
    return tier.getId();
  }

  @Override
  public String getRoleId() {
    return tier.getRoleId();
  }

  @Override
  public LocalDate getExpireDate() {
    return tier.getExpireDate();
  }

  @Override
  public RoleModel getRole() {
    return role;
  }

  @Override
  public String getRoleName() {
    return role.getName();
  }
}
