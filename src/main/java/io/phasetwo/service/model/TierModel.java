package io.phasetwo.service.model;

import java.time.LocalDate;
import org.keycloak.models.RoleModel;

public interface TierModel {
  String getId();

  String getRoleId();

  LocalDate getExpireDate();

  RoleModel getRole();

  String getRoleName();
}
