package io.phasetwo.service.model;

import java.util.stream.Stream;
import org.keycloak.models.UserModel;

public interface OrganizationRoleModel {

  String getId();

  String getName();

  void setName(String name);

  String getDescription();

  void setDescription(String description);

  default Stream<UserModel> getUserMappingsStream() {
    return getUserMappingsStream(false);
  }

  Stream<UserModel> getUserMappingsStream(boolean excludeAdminAccounts);

  void grantRole(UserModel user);

  void revokeRole(UserModel user);

  boolean hasRole(UserModel user);
}
