package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Organization {

  private @Valid String id = null;
  private @Valid String name = null;
  private @Valid String displayName = null;
  private @Valid String url = null;
  private @Valid String realm = null;
  private @Valid Set<String> domains = Sets.newHashSet();
  private @Valid Map<String, List<String>> attributes = Maps.newHashMap();

  public Organization id(String id) {
    this.id = id;
    return this;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Organization name(String name) {
    this.name = name;
    return this;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Organization displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Organization domains(Set<String> domains) {
    this.domains = domains;
    return this;
  }

  @JsonProperty("domains")
  public Set<String> getDomains() {
    return domains;
  }

  public void setDomains(Set<String> domains) {
    this.domains = domains;
  }

  public Organization url(String url) {
    this.url = url;
    return this;
  }

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  /** */
  public Organization realm(String realm) {
    this.realm = realm;
    return this;
  }

  @JsonProperty("realm")
  public String getRealm() {
    return realm;
  }

  public void setRealm(String realm) {
    this.realm = realm;
  }

  public Organization attribute(String name, String value) {
    List<String> list = this.attributes.get(name);
    if (list == null) {
      list = Lists.newArrayList();
    }
    if (!list.contains(value)) list.add(value);
    this.attributes.put(name, list);
    return this;
  }

  @JsonProperty("attributes")
  public Map<String, List<String>> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, List<String>> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Organization organization = (Organization) o;
    return Objects.equals(id, organization.id)
        && Objects.equals(name, organization.name)
        && Objects.equals(displayName, organization.displayName)
        && Objects.equals(domains, organization.domains)
        && Objects.equals(url, organization.url)
        && Objects.equals(realm, organization.realm)
        && Objects.equals(attributes, organization.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, displayName, domains, url, realm, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Organization {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    domains: ").append(toIndentedString(domains)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    realm: ").append(toIndentedString(realm)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
