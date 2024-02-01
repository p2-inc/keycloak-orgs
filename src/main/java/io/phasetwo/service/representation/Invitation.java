package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.validation.Valid;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class Invitation {
  private @Valid String id = null;
  private @Valid String email = null;
  private @Valid String createdAt = null;
  private @Valid String inviterId = null;
  private @Valid String invitationUrl = null;
  private @Valid String organizationId = null;
  private @Valid List<String> teamIds = Lists.newArrayList();
  private @Valid List<String> roles = Lists.newArrayList();
  private @Valid Map<String, List<String>> attributes = Maps.newHashMap();

  public Invitation id(String id) {
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

  public Invitation email(String email) {
    this.email = email;
    return this;
  }

  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  /** ISO 8601 format: YYYY-MM-DDTHH:MM:SSZ */
  public Invitation createdAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public Invitation createdAt(Date createdAt) {
    setCreatedAt(createdAt);
    return this;
  }

  @JsonProperty("createdAt")
  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  @JsonIgnore
  public void setCreatedAt(Date createdAt) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df =
        new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    df.setTimeZone(tz);
    this.createdAt = df.format(createdAt);
  }

  public Invitation inviterId(String inviterId) {
    this.inviterId = inviterId;
    return this;
  }

  @JsonProperty("inviterId")
  public String getInviterId() {
    return inviterId;
  }

  public void setInviterId(String inviterId) {
    this.inviterId = inviterId;
  }

  public Invitation invitationUrl(String invitationUrl) {
    this.invitationUrl = invitationUrl;
    return this;
  }

  @JsonProperty("invitationUrl")
  public String getInvitationUrl() {
    return invitationUrl;
  }

  public void setInvitationUrl(String invitationUrl) {
    this.invitationUrl = invitationUrl;
  }

  public Invitation organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }

  @JsonProperty("organizationId")
  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public Invitation teamId(String teamId) {
    if (teamIds == null) {
      teamIds = Lists.newArrayList();
    }
    if (!teamIds.contains(teamId)) teamIds.add(teamId);
    return this;
  }

  public Invitation teamIds(List<String> teamIds) {
    this.teamIds = teamIds;
    return this;
  }

  @JsonProperty("teams")
  @JsonIgnore // ignore field "teams" because it is not present in InvitationRepresentation (class
  // io.phasetwo.client.openapi.model.InvitationRepresentation)
  public List<String> getTeamIds() {
    return teamIds;
  }

  public void setTeamIds(List<String> teamIds) {
    this.teamIds = teamIds;
  }

  public Invitation role(String role) {
    if (roles == null) {
      roles = Lists.newArrayList();
    }
    if (!roles.contains(role)) roles.add(role);
    return this;
  }

  public Invitation roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public Invitation attribute(String name, String value) {
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
    Invitation invitation = (Invitation) o;

    return Objects.equals(id, invitation.id)
        && Objects.equals(email, invitation.email)
        && Objects.equals(createdAt, invitation.createdAt)
        && Objects.equals(inviterId, invitation.inviterId)
        && Objects.equals(organizationId, invitation.organizationId)
        && Objects.equals(invitationUrl, invitation.invitationUrl)
        && Objects.equals(roles, invitation.roles)
        && Objects.equals(teamIds, invitation.teamIds)
        && Objects.equals(attributes, invitation.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, email, createdAt, inviterId, organizationId, invitationUrl, roles, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Invitation {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    inviterId: ").append(toIndentedString(inviterId)).append("\n");
    sb.append("    organizationId: ").append(toIndentedString(organizationId)).append("\n");
    sb.append("    invitationUrl: ").append(toIndentedString(invitationUrl)).append("\n");
    sb.append("    teamIds: ").append(toIndentedString(teamIds)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
