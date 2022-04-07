package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;

import io.phasetwo.service.representation.Organization;
import java.util.stream.Stream;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

/** */
@JBossLog
public class UserResource extends OrganizationAdminResource {

  public UserResource(RealmModel realm) {
    super(realm);
  }

  ////////
  // Users
  ////////

  @GET
  @Path("/{userId}/orgs")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Organization> listUserOrgs(@PathParam("userId") String userId) {
    log.infov("Get org memberships for %s %s", realm.getName(), userId);

    UserModel user = session.users().getUserById(realm, userId);
    return orgs.getUserOrganizationsStream(realm, user)
        .filter(m -> (auth.hasViewOrgs() || auth.hasOrgViewOrg(m)))
        .map(m -> convertOrganizationModelToOrganization(m));
  }

  /*
  teams is on hold for now

    @GET
    @Path("/{userId}/teams")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUserTeams(@PathParam("userId") String userId) {
      log.infov("Get team memberships for %s %s", realm.getName(), userId);
      Teams teams =
          mgr.getTeamsByUserId(userId).stream()
              .map(e -> convertTeamEntityToTeam(e))
              .collect(Collectors.toCollection(() -> new Teams()));
      return Response.ok().entity(teams).build();
    }
    */
}
