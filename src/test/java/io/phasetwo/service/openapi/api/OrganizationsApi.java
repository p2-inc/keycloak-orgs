package io.phasetwo.service.openapi.api;

import io.phasetwo.service.representation.Organization;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/{realm}/orgs")
public interface OrganizationsApi {

    @POST
    @Consumes({ "application/json" })
    Response createOrganization(@PathParam("realm") String realm, Organization organizationRepresentation);

    @POST
    @Path("/{orgId}/portal-link")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json" })
    Response createPortalLink(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@FormParam(value = "userId")  String userId);

    @DELETE
    @Path("/{orgId}")
    Response deleteOrganization(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @GET
    @Path("/{orgId}")
    @Produces({ "application/json" })
    Response  getOrganizationById(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @GET
    @Produces({ "application/json" })
    Response getOrganizations(@PathParam("realm") String realm,@QueryParam("search")   String search,@QueryParam("first")   Integer first,@QueryParam("max")   Integer max);

    @PUT
    @Path("/{orgId}")
    @Consumes({ "application/json" })
    Response updateOrganization(@PathParam("realm") String realm,@PathParam("orgId") String orgId,Organization organizationRepresentation);
}
