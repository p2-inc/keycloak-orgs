package io.phasetwo.service.model;

import org.keycloak.models.RealmModel;

public interface OrganizationsConfigModel {
  String getId();

  boolean isCreateAdminUser();

  boolean isSharedIdps();

  void setCreateAdminUser(boolean createAdminUser);

  void setSharedIdps(boolean sharedIdps);

  RealmModel getRealm();
}
