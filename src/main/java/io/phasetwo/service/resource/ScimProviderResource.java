package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.keycloak.orgs.scim.ComponentScimConfig;
import io.phasetwo.keycloak.orgs.scim.spi.ScimConfigurationProvider;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.OrganizationScimRepresentation;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.events.admin.OperationType;

@JBossLog
public class ScimProviderResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public ScimProviderResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  private ScimConfigurationProvider getConfigProvider() {
    return session.getProvider(ScimConfigurationProvider.class);
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getScimConfig() {
    if (!auth.hasViewOrgs() && !auth.hasOrgViewIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to view SCIM config for %s", organization.getId()));
    }

    ComponentScimConfig config = getConfigProvider().getConfiguration(organization.getId());
    if (config == null) {
      throw new NotFoundException("No SCIM configuration found for organization");
    }

    OrganizationScimRepresentation rep = convertScimConfigToRepresentation(config);
    return Response.ok(rep).build();
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createScimConfig(OrganizationScimRepresentation rep) {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to manage SCIM config for %s", organization.getId()));
    }

    ScimConfigurationProvider configProvider = getConfigProvider();
    if (configProvider.hasConfiguration(organization.getId())) {
      return Response.status(Response.Status.CONFLICT)
          .entity("SCIM configuration already exists for this organization")
          .build();
    }

    ComponentScimConfig newConfig =
        convertRepresentationToScimConfig(rep);
    ComponentScimConfig created =
        configProvider.createConfiguration(organization.getId(), newConfig);

    OrganizationScimRepresentation createdRep = convertScimConfigToRepresentation(created);

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.CREATE)
        .resourcePath(session.getContext().getUri())
        .representation(createdRep)
        .success();

    return Response.status(Response.Status.CREATED).entity(createdRep).build();
  }

  @PUT
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateScimConfig(OrganizationScimRepresentation rep) {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to manage SCIM config for %s", organization.getId()));
    }

    ScimConfigurationProvider configProvider = getConfigProvider();
    if (!configProvider.hasConfiguration(organization.getId())) {
      throw new NotFoundException("No SCIM configuration found for organization");
    }

    ComponentScimConfig updateConfig =
        convertRepresentationToScimConfig(rep);
    ComponentScimConfig updated =
        configProvider.updateConfiguration(organization.getId(), updateConfig);

    OrganizationScimRepresentation updatedRep = convertScimConfigToRepresentation(updated);

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.UPDATE)
        .resourcePath(session.getContext().getUri())
        .representation(updatedRep)
        .success();

    return Response.ok(updatedRep).build();
  }

  @DELETE
  @Path("")
  public Response deleteScimConfig() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to manage SCIM config for %s", organization.getId()));
    }

    ScimConfigurationProvider configProvider = getConfigProvider();
    if (!configProvider.hasConfiguration(organization.getId())) {
      throw new NotFoundException("No SCIM configuration found for organization");
    }

    configProvider.deleteConfiguration(organization.getId());

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri())
        .representation(organization.getId())
        .success();

    return Response.noContent().build();
  }
}
