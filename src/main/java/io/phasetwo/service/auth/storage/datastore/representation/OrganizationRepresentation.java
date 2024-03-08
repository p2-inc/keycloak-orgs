package io.phasetwo.service.auth.storage.datastore.representation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationRepresentation {

    @JsonProperty("organization")
    private Organization organization;

    @JsonProperty("roles")
    private List<OrganizationRole> roles = new ArrayList<>();

    @JsonProperty("idpLink")
    private LinkIdp idpLink;

    @JsonProperty("members")
    private List<UserRolesRepresentation> members = new ArrayList<>();

    @JsonProperty("invitations")
    private List<Invitation> invitations = new ArrayList<>();
}
