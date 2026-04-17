package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicAuthScimAuth extends OrganizationScimAuth {

  @JsonProperty("username")
  private String username;

  @JsonProperty("password")
  private String password;

  @Override
  public String getType() {
    return "EXTERNAL_BASIC";
  }
}
