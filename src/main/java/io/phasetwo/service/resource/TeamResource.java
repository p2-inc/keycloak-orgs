package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.KeycloakSession;

/** */
@JBossLog
public class TeamResource extends OrganizationAdminResource {

  public TeamResource(KeycloakSession session) {
    super(session);
  }

  ////////
  // Teams
  ////////

  /*
  //teams are on hold for now

    @DELETE
    @Path("/{teamId}")
    public Response deleteTeam(@PathParam("teamId") String teamId) {
      log.debugv("Delete team for %s %s", realm.getName(), teamId);

      TeamEntity removed = mgr.removeTeamById(teamId);

      adminEvent
          .resource(TEAM.name())
          .operation(OperationType.DELETE)
          .resourcePath(session.getContext().getUri(), removed.getId())
          .representation(convertTeamEntityToTeam(removed))
          .success();

      return Response.status(204).build();
    }

    @GET
    @Path("/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeam(@PathParam("teamId") String teamId) {
      log.debugv("Get team for %s %s", realm.getName(), teamId);

      return Response.ok().entity(convertTeamEntityToTeam(mgr.getTeamById(teamId))).build();
    }

    @GET
    @Path("/{teamId}/members")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTeamMembers(
        @PathParam("teamId") String teamId,
        @QueryParam("first") Integer firstResult,
        @QueryParam("max") Integer maxResults) {
      log.debugv("Get members for %s %s", realm.getName(), teamId);

      firstResult = firstResult != null ? firstResult : -1;
      maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
      Users users =
          mgr
              .getTeamMembersById(
                  teamId, Optional.ofNullable(firstResult), Optional.ofNullable(maxResults))
              .stream()
              .map(e -> convertUserEntityToUserRepresentation(e))
              .collect(Collectors.toCollection(() -> new Users()));
      return Response.ok().entity(users).build();
    }

    @DELETE
    @Path("/{teamId}/members/{userId}")
    public Response removeTeamMember(
        @PathParam("teamId") String teamId, @PathParam("userId") String userId) {
      log.debugv("Remove member %s from %s %s", userId, realm.getName(), teamId);

      int removed = mgr.removeTeamMemberById(teamId, userId);

      if (removed > 0) {
        adminEvent
            .resource(TEAM_MEMBERSHIP.name())
            .operation(OperationType.DELETE)
            .resourcePath(session.getContext().getUri())
            .representation(userId)
            .success();
        return Response.status(204).build();
      } else {
        return Response.status(404).build();
      }
    }

    @GET
    @Path("/{teamId}/members/{userId}")
    public Response getTeamMember(
        @PathParam("teamId") String teamId, @PathParam("userId") String userId) {
      log.debugv("Check membership %s for %s %s", userId, realm.getName(), teamId);
      if (mgr.getTeamMemberById(teamId, userId) != null) return Response.status(204).build();
      else return Response.status(404).build();
    }

    @PUT
    @Path("/{teamId}/members/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTeamMember(
        @PathParam("teamId") String teamId, @PathParam("userId") String userId) {
      log.debugv("Add %s as member for %s %s", userId, realm.getName(), teamId);

      if (mgr.addMemberToTeamById(teamId, userId) > 0) {
        adminEvent
            .resource(TEAM_MEMBERSHIP.name())
            .operation(OperationType.CREATE)
            .resourcePath(session.getContext().getUri())
            .representation(userId)
            .success();
        return Response.status(204).build();
      } else {
        return Response.status(404).build();
      }
    }

    @PUT
    @Path("/{teamId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTeam(@Valid Team body, @PathParam("teamId") String teamId) {
      log.debugf("Update team for %s %s", realm.getName(), teamId);

      TeamEntity e = mgr.updateTeam(teamId, body);
      Team o = convertTeamEntityToTeam(e);

      adminEvent
          .resource(TEAM.name())
          .operation(OperationType.UPDATE)
          .resourcePath(session.getContext().getUri(), e.getId())
          .representation(o)
          .success();

      return Response.ok().entity(o).build();
    }
    */
}
