package io.phasetwo.service.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.*;

public class InvitationRequest {
  private @Email @Valid String email = null;
  private @Valid String inviterId = null;

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
        && Objects.equals(inviterId, invitationRequest.inviterId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, inviterId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvitationRequest {\n");

    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    inviterId: ").append(toIndentedString(inviterId)).append("\n");
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
