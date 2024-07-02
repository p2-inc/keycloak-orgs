package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.util.IdentityProviders;
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
    IdentityProviders.removeOrganization(organization.getId(), idp);

    realm.updateIdentityProvider(idp);
    return Response.noContent().build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(IdentityProviderRepresentation providerRep) {
    requireManage();
    IdentityProviderModel idp = realm.getIdentityProviderByAlias(alias);
    if (idp == null) {
      throw new NotFoundException(String.format("No IdP found with alias %s", alias));
    }
    var orgs = IdentityProviders.getAttributeMultivalued(idp.getConfig(), ORG_OWNER_CONFIG_KEY);

    // don't allow override of ownership and other conf vars
    providerRep.getConfig().put("hideOnLoginPage", "true");
    IdentityProviders.setAttributeMultivalued(providerRep.getConfig(), ORG_OWNER_CONFIG_KEY, orgs);
    providerRep.getConfig().put(ORG_SHARED_IDP_KEY, idp.getConfig().get(ORG_SHARED_IDP_KEY));

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
