package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JwtScimAuth extends OrganizationScimAuth {

  @JsonProperty("issuer")
  private String issuer;

  @JsonProperty("audience")
  private String audience;

  @JsonProperty("jwks_uri")
  private String jwksUri;

  @Override
  public String getType() {
    return "EXTERNAL_JWT";
  }
}
