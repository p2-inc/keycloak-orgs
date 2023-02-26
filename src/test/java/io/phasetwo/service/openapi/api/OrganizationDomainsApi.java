package io.phasetwo.service.openapi.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/{realm}/orgs/{orgId}/domains")
public interface OrganizationDomainsApi {

    @GET
    @Path("/{domainName}")
    @Produces({ "application/json" })
    Response getOrganizationDomain(@PathParam("realm") String realm, @PathParam("orgId") String orgId, @PathParam("domainName") String domainName);

    @GET
    @Produces({ "application/json" })
    Response getOrganizationDomains(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @POST
    @Path("/{domainName}/verify")
    Response verifyDomain(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("domainName") String domainName);
}
