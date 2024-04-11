package io.phasetwo.service.auth.invitation;

import static io.phasetwo.service.Orgs.FIELD_ORG_ID;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

public class InvitationLinkActionToken extends DefaultActionToken {

  public static final String TOKEN_TYPE = "ext-org-invitation-link";

  private static final String JSON_FIELD_REDIRECT_URI = "rdu";

  @JsonProperty(value = FIELD_ORG_ID)
  private String orgId;

  @JsonProperty(value = JSON_FIELD_REDIRECT_URI)
  private String redirectUri;

  public InvitationLinkActionToken(
      String email,
      int absoluteExpirationInSecs,
      String orgId,
      String clientId,
      String redirectUri) {
    super(email, TOKEN_TYPE, absoluteExpirationInSecs, null);
    this.orgId = orgId;
    this.redirectUri = redirectUri;
    this.issuedFor = clientId;
  }

  private InvitationLinkActionToken() {
    // Note that the class must have a private constructor without any arguments. This is necessary
    // to deserialize the token class from JWT.
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }
}
