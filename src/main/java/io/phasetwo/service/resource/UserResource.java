package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.representation.OrganizationRole;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/** */
@JBossLog
public class UserResource extends OrganizationAdminResource {

  public UserResource(KeycloakSession session) {
    super(session);
  }

  ////////
  // Users
  ////////

  @GET
  @Path("/{userId}/orgs")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Organization> listUserOrgs(@PathParam("userId") String userId) {
    log.debugv("Get org memberships for %s %s", realm.getName(), userId);

    UserModel user = session.users().getUserById(realm, userId);
    return orgs.getUserOrganizationsStream(realm, user)
        .filter(m -> (auth.hasViewOrgs() || auth.hasOrgViewOrg(m)))
        .map(m -> convertOrganizationModelToOrganization(m));
  }

  @GET
  @Path("/{userId}/orgs/{orgId}/roles")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<OrganizationRole> listUserOrgRoles(
      @PathParam("userId") String userId, @PathParam("orgId") String orgId) {
    log.debugv("Get org roles for %s %s %s", realm.getName(), userId, orgId);

    UserModel user = session.users().getUserById(realm, userId);
    OrganizationModel org = orgs.getOrganizationById(realm, orgId);
    if (auth.hasViewOrgs() || auth.hasOrgViewRoles(org)) {
      if (org.hasMembership(user)) {
        return org.getRolesStream()
            .filter(r -> r.hasRole(user))
            .map(r -> convertOrganizationRole(r));
      } else {
        throw new NotFoundException("User is not a member of the organization");
      }
    } else {
      throw new NotAuthorizedException("Insufficient permissions");
    }
  }

  /*
  teams is on hold for now

    @GET
    @Path("/{userId}/teams")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUserTeams(@PathParam("userId") String userId) {
      log.debugv("Get team memberships for %s %s", realm.getName(), userId);
      Teams teams =
          mgr.getTeamsByUserId(userId).stream()
              .map(e -> convertTeamEntityToTeam(e))
              .collect(Collectors.toCollection(() -> new Teams()));
      return Response.ok().entity(teams).build();
    }
    */
}
