package io.phasetwo.service.openapi.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/{realm}")
public interface UsersApi {

    @GET
    @Path("/users/{userId}/orgs")
    @Produces({ "application/json" })
    Response realmUsersUserIdOrgsGet(@PathParam("realm") String realm,@PathParam("userId") String userId);

    @GET
    @Path("/users/{userId}/orgs/{orgId}/roles")
    @Produces({ "application/json" })
    Response realmUsersUserIdOrgsOrgIdRolesGet(@PathParam("realm") String realm,@PathParam("userId") String userId,@PathParam("orgId") String orgId);
}
