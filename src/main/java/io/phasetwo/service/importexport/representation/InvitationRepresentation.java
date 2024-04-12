package io.phasetwo.service.importexport.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvitationRepresentation {
  @JsonProperty("email")
  private String email;

  @JsonProperty("inviterUsername")
  private String inviterUsername;

  @JsonProperty("roles")
  private List<String> roles = Lists.newArrayList();

  @JsonProperty("redirectUri")
  private String redirectUri;

  @JsonProperty("attributes")
  private Map<String, List<String>> attributes = Maps.newHashMap();
}
