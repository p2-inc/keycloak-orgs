package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InvitationRequest {
  private @Email @Valid String email = null;
  private @Valid String inviterId = null;
  private boolean send = false;
  private @Valid List<String> roles = Lists.newArrayList();
  private String redirectUri = null;
  private @Valid Map<String, List<String>> attributes = Maps.newHashMap();

  public InvitationRequest email(String email) {
    this.email = email;
    return this;
  }

  @JsonProperty("email")
  @NotNull
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public InvitationRequest inviterId(String inviterId) {
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

  public InvitationRequest send(boolean send) {
    this.send = send;
    return this;
  }

  @JsonProperty("send")
  public boolean isSend() {
    return send;
  }

  public void setSend(boolean send) {
    this.send = send;
  }

  public InvitationRequest redirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

  @JsonProperty("redirectUri")
  @NotNull
  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  public InvitationRequest role(String role) {
    if (roles == null) {
      roles = Lists.newArrayList();
    }
    if (!roles.contains(role)) roles.add(role);
    return this;
  }

  public InvitationRequest roles(List<String> roles) {
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

  public InvitationRequest attribute(String name, String value) {
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
    InvitationRequest invitationRequest = (InvitationRequest) o;
    return Objects.equals(email, invitationRequest.email)
        && Objects.equals(inviterId, invitationRequest.inviterId)
        && Objects.equals(send, invitationRequest.send)
        && Objects.equals(redirectUri, invitationRequest.redirectUri)
        && Objects.equals(roles, invitationRequest.roles)
        && Objects.equals(roles, invitationRequest.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, inviterId, send, redirectUri, roles, attributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvitationRequest {\n");

    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    inviterId: ").append(toIndentedString(inviterId)).append("\n");
    sb.append("    send: ").append(toIndentedString(send)).append("\n");
    sb.append("    redirectUri: ").append(toIndentedString(redirectUri)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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
