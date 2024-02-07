package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;

public class SwitchOrganization {
  private @Valid String id = null;

  public SwitchOrganization id(String id) {
    this.id = id;
    return this;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }
}
