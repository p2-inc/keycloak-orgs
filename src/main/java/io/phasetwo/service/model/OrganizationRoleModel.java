package io.phasetwo.service.model;

import java.util.stream.Stream;
import org.keycloak.models.UserModel;

public interface OrganizationRoleModel {

  String getId();

  String getName();

  void setName(String name);

  String getDescription();

  void setDescription(String description);

  Stream<UserModel> getUserMappingsStream();

  void grantRole(UserModel user);

  void revokeRole(UserModel user);

  boolean hasRole(UserModel user);
}
