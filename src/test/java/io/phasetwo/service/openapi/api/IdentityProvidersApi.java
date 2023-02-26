package io.phasetwo.service.openapi.api;

import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Map;


@Path("/{realm}/orgs/{orgId}/idps")
public interface IdentityProvidersApi {

    @POST
    @Path("/{alias}/mappers")
    @Consumes({ "application/json" })
    Response addIdpMapper(@PathParam("realm") String realm, @PathParam("orgId") String orgId, @PathParam("alias") String alias, IdentityProviderMapperRepresentation identityProviderMapperRepresentation);

    @POST
    @Consumes({ "application/json" })
    Response createIdp(@PathParam("realm") String realm, @PathParam("orgId") String orgId, IdentityProviderRepresentation identityProviderRepresentation);

    @DELETE
    @Path("/{alias}")
    Response deleteIdp(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias);

    @DELETE
    @Path("/{alias}/mappers/{id}")
    Response deleteIdpMapper(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias,@PathParam("id") String id);

    @GET
    @Path("/{alias}")
    @Produces({ "application/json" })
    Response getIdp(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias);

    @GET
    @Path("/{alias}/mappers/{id}")
    @Produces({ "application/json" })
    Response getIdpMapper(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias,@PathParam("id") String id);

    @GET
    @Path("/{alias}/mappers")
    @Produces({ "application/json" })
    Response getIdpMappers(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias);

    @GET
    @Produces({ "application/json" })
    Response getIdps(@PathParam("realm") String realm,@PathParam("orgId") String orgId);

    @POST
    @Path("/import-config")
    @Produces({ "application/json" })
    @Consumes({ "application/json" })
    Response importIdpJson(@PathParam("realm") String realm,@PathParam("orgId") String orgId,Map<String, String> body);

    @PUT
    @Path("/{alias}")
    @Consumes({ "application/json" })
    Response updateIdp(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias,IdentityProviderRepresentation identityProviderRepresentation);

    @PUT
    @Path("/{alias}/mappers/{id}")
    @Consumes({ "application/json" })
    Response updateIdpMapper(@PathParam("realm") String realm,@PathParam("orgId") String orgId,@PathParam("alias") String alias,@PathParam("id") String id,IdentityProviderMapperRepresentation identityProviderMapperRepresentation);
}
