package io.phasetwo.service.datastore.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRepresentation {

  @JsonProperty("organization")
  private OrganizationAttributes organization;

  @JsonProperty("roles")
  private List<OrganizationRoleRepresentation> roles = new ArrayList<>();

  @JsonProperty("idpLink")
  private String idpLink;

  @JsonProperty("members")
  private List<UserRolesRepresentation> members = new ArrayList<>();

  @JsonProperty("invitations")
  private List<InvitationRepresentation> invitations = new ArrayList<>();
}
