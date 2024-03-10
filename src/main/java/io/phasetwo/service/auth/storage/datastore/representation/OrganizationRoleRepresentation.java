package io.phasetwo.service.auth.storage.datastore.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRoleRepresentation {

  @JsonProperty("name")
  private @NotNull @Valid String name;
  @JsonProperty("description")
  private String description;
}
