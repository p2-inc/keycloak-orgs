package io.phasetwo.service.auth.storage.datastore.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationAttributes {

  @JsonProperty("name")
  private @NotNull @Valid String name;
  @JsonProperty("displayName")
  private @Valid String displayName;
  @JsonProperty("url")
  private @Valid String url;
  @JsonProperty("domains")
  private @Valid Set<String> domains = Sets.newHashSet();
  @JsonProperty("attributes")
  private @Valid Map<String, List<String>> attributes = Maps.newHashMap();
}
