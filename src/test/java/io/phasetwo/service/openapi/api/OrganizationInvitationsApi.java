package io.phasetwo.service.openapi.api;

import io.phasetwo.service.representation.InvitationRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/{realm}/orgs/{orgId}/invitations")
public interface OrganizationInvitationsApi {

    @POST
    @Consumes({ "application/json" })
    Response addOrganizationInvitation(@PathParam("realm") String realm, @PathParam("orgId") String orgId, InvitationRequest invitationRequestRepresentation);

    @GET
    @Produces({ "application/json" })
    Response getOrganizationInvitations(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @GET
    @Produces({ "application/json" })
    Response getOrganizationInvitations(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@QueryParam("search")   String search,@QueryParam("first")   Integer first,@QueryParam("max")   Integer max);

    @DELETE
    @Path("/{invitationId}")
    Response removeOrganizationInvitation(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("invitationId") String invitationId);
}
