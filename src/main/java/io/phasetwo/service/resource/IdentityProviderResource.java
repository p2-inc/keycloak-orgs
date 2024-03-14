package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.*;
import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

@JBossLog
public class IdentityProviderResource extends OrganizationAdminResource {

  private final OrganizationModel organization;
  private final String alias;
  private final org.keycloak.services.resources.admin.IdentityProviderResource kcResource;

  public IdentityProviderResource(
      OrganizationAdminResource parent,
      OrganizationModel organization,
      String alias,
      org.keycloak.services.resources.admin.IdentityProviderResource kcResource) {
    super(parent);
    this.organization = organization;
    this.alias = alias;
    this.kcResource = kcResource;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public IdentityProviderRepresentation getIdentityProvider() {
    return kcResource.getIdentityProvider();
  }

  @DELETE
  public Response delete() {
    requireManage();
    return kcResource.delete();
  }

  @POST
  @Path("unlink")
  public Response unlinkIdp() {
    // authz
    if (!auth.hasManageOrgs()) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to unlink identity provider for %s", organization.getId()));
    }

    // get an idp with the same alias
    IdentityProviderModel idp = realm.getIdentityProviderByAlias(alias);
    if (idp == null) {
      throw new NotFoundException(String.format("No IdP found with alias %s", alias));
    }

    idp.getConfig().remove(ORG_OWNER_CONFIG_KEY);
    realm.updateIdentityProvider(idp);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(IdentityProviderRepresentation providerRep) {
    requireManage();
    // don't allow override of ownership and other conf vars
    providerRep.getConfig().put("hideOnLoginPage", "true");
    providerRep.getConfig().put(ORG_OWNER_CONFIG_KEY, organization.getId());
    // force alias
    providerRep.setAlias(alias);

    return kcResource.update(providerRep);
  }

  @GET
  @Path("mappers")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<IdentityProviderMapperRepresentation> getMappers() {
    return kcResource.getMappers();
  }

  @POST
  @Path("mappers")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addMapper(IdentityProviderMapperRepresentation mapper) {
    requireManage();
    return kcResource.addMapper(mapper);
  }

  @GET
  @Path("mappers/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public IdentityProviderMapperRepresentation getMapperById(@PathParam("id") String id) {
    return kcResource.getMapperById(id);
  }

  @PUT
  @Path("mappers/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public void update(@PathParam("id") String id, IdentityProviderMapperRepresentation rep) {
    requireManage();
    kcResource.update(id, rep);
  }

  @DELETE
  @Path("mappers/{id}")
  public void delete(@PathParam("id") String id) {
    requireManage();
    kcResource.delete(id);
  }

  private void requireManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to manage identity providers for %s", organization.getId()));
    }
  }
}
