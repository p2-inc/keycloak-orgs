package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public class Domain {

  private String domainName;
  private String recordKey;
  private String recordValue;
  private boolean verified = false;

  public Domain domainName(String domainName) {
    this.domainName = domainName;
    return this;
  }

  @JsonProperty("domain_name")
  @NotNull
  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String domainName) {
    this.domainName = domainName;
  }

  public Domain recordKey(String recordKey) {
    this.recordKey = recordKey;
    return this;
  }

  @JsonProperty("record_key")
  public String getRecordKey() {
    return recordKey;
  }

  public void setRecordKey(String recordKey) {
    this.recordKey = recordKey;
  }

  public Domain recordValue(String recordValue) {
    this.recordValue = recordValue;
    return this;
  }

  @JsonProperty("record_value")
  public String getRecordValue() {
    return recordValue;
  }

  public void setRecordValue(String recordValue) {
    this.recordValue = recordValue;
  }

  public Domain verified(boolean verified) {
    this.verified = verified;
    return this;
  }

  @JsonProperty("verified")
  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }
}
