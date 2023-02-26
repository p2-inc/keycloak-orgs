package io.phasetwo.service.openapi.api;

import io.phasetwo.service.representation.OrganizationRole;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/{realm}/orgs/{orgId}/roles")
public interface OrganizationRolesApi {

    @GET
    @Path("/{name}/users/{userId}")
    Response checkUserOrganizationRole(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name,@PathParam("userId") String userId);

    @POST
    @Consumes({ "application/json" })
    Response createOrganizationRole(@PathParam("realm") String realm, @PathParam("orgId") String orgId, OrganizationRole organizationRoleRepresentation);

    @DELETE
    @Path("/{name}")
    Response deleteOrganizationRole(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name);

    @GET
    @Path("/{name}")
    @Produces({ "application/json" })
    Response getOrganizationRole(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name);

    @GET
    @Produces({ "application/json" })
    Response getOrganizationRoles(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @GET
    @Path("/{name}/users")
    @Produces({ "application/json" })
    Response getUserOrganizationRoles(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name);

    @PUT
    @Path("/{name}/users/{userId}")
    Response grantUserOrganizationRole(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name,@PathParam("userId") String userId);

    @DELETE
    @Path("/{name}/users/{userId}")
    Response revokeUserOrganizationRole(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name,@PathParam("userId") String userId);

    @PUT
    @Path("/{name}")
    @Consumes({ "application/json" })
    Response updateOrganizationRole(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("name") String name,OrganizationRole organizationRoleRepresentation);
}
