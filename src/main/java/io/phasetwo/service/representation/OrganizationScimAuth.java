package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = KeycloakScimAuth.class, name = "KEYCLOAK"),
  @JsonSubTypes.Type(value = JwtScimAuth.class, name = "EXTERNAL_JWT"),
  @JsonSubTypes.Type(value = SharedSecretScimAuth.class, name = "EXTERNAL_SECRET"),
  @JsonSubTypes.Type(value = BasicAuthScimAuth.class, name = "EXTERNAL_BASIC")
})
public abstract class OrganizationScimAuth {

  @JsonProperty("type")
  public abstract String getType();
}
