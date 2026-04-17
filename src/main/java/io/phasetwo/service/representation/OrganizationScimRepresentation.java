package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationScimRepresentation {

  @JsonProperty("enabled")
  private Boolean enabled;

  @JsonProperty("email_as_username")
  private Boolean emailAsUsername;

  @JsonProperty("link_idp")
  private Boolean linkIdp;

  @JsonProperty("auth")
  private OrganizationScimAuth auth;
}
