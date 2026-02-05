package io.phasetwo.service.importexport.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.phasetwo.service.util.EmptyStringAsNullDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationAttributes {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("displayName")
  private String displayName;

  @JsonProperty("url")
  private String url;

  @JsonProperty("domains")
  @JsonDeserialize(contentUsing = EmptyStringAsNullDeserializer.class)
  @JsonSetter(contentNulls = Nulls.SKIP)
  private Set<String> domains = Sets.newHashSet();

  @JsonProperty("attributes")
  private Map<String, List<String>> attributes = Maps.newHashMap();
}
