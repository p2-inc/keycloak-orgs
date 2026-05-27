package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SharedSecretScimAuth extends OrganizationScimAuth {

  @JsonProperty("shared_secret")
  private String sharedSecret;

  @Override
  public String getType() {
    return "EXTERNAL_SECRET";
  }
}
