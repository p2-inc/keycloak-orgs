package io.phasetwo.service.openapi.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/{realm}/orgs/{orgId}/members")
public interface OrganizationMembershipsApi {

    @PUT
    @Path("/{userId}")
    Response addOrganizationMember(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("userId") String userId);

    @GET
    @Path("/{userId}")
    Response checkOrganizationMembership(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("userId") String userId);

    @GET
    @Produces({ "application/json" })
    Response getOrganizationMemberships(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @GET
    @Produces({ "application/json" })
    Response getOrganizationMemberships(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@QueryParam("first")   Integer first,@QueryParam("max")   Integer max);

    @DELETE
    @Path("/{userId}")
    Response removeOrganizationMember(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("userId") String userId);
}
