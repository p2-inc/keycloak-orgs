package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkIdp {

  @JsonProperty("alias")
  private String alias;

  @JsonProperty("post_broker_flow")
  private String postBrokerFlow;

  @JsonProperty("sync_mode")
  private String syncMode;
}
