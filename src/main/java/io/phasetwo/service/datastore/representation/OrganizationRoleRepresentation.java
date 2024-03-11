package io.phasetwo.service.datastore.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRoleRepresentation {

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;
}
