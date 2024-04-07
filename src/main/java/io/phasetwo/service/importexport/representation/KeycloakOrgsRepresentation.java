package io.phasetwo.service.importexport.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class KeycloakOrgsRepresentation {

  @JsonProperty("organizations")
  private List<OrganizationRepresentation> organizations;
}
