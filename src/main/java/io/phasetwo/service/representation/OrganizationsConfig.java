package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationsConfig {
  @JsonProperty("createAdminUserEnabled")
  private boolean createAdminUser = true;

  @JsonProperty("sharedIdpsEnabled")
  private boolean sharedIdps = false;

  @JsonProperty("multipleIdpsEnabled")
  private boolean multipleIdps = false;

  @JsonProperty("expirationInSecs")
  private int expirationInSecs = 86400; // 1 day

  @JsonProperty("defaultApplicationUri")
  private String defaultApplicationUri = null;
  
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

  public boolean isMultipleIdps() {
    return multipleIdps;
  }

  public void setMultipleIdps(boolean multipleIdps) {
    this.multipleIdps = multipleIdps;
  }

  public int getExpirationInSecs() {
    return expirationInSecs;
  }

  public void setExpirationInSecs(int expirationInSecs) {
    this.expirationInSecs = expirationInSecs;
  }

  public String getDefaultApplicationUri() {
    return defaultApplicationUri;
  }

  public void setDefaultApplicationUri(String defaultApplicationUri) {
    this.defaultApplicationUri = defaultApplicationUri;
  }
}
