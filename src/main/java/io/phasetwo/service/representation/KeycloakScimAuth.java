package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeycloakScimAuth extends OrganizationScimAuth {

  @Override
  public String getType() {
    return "KEYCLOAK";
  }
}
