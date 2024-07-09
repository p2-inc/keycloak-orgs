package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationsConfig {
  @JsonProperty("createAdminUserEnabled")
  private boolean createAdminUser = true;

  @JsonProperty("sharedIdpsEnabled")
  private boolean sharedIdps = false;

  public boolean isCreateAdminUser() {
    return createAdminUser;
  }

  public void setCreateAdminUser(boolean createAdminUser) {
    this.createAdminUser = createAdminUser;
  }

  public boolean isSharedIdps() {
    return sharedIdps;
  }

  public void setSharedIdps(boolean sharedIdps) {
    this.sharedIdps = sharedIdps;
  }
}
