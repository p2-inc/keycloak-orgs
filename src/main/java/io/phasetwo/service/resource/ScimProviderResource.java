package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.OrganizationScimRepresentation;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.component.ComponentModel;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.RealmModel;

@JBossLog
public class ScimProviderResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public ScimProviderResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
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

    RealmModel realm = session.getContext().getRealm();
    ComponentModel component = realm.getComponent(organization.getId());
    if (component == null) {
      throw new NotFoundException("No SCIM configuration found for organization");
    }

    OrganizationScimRepresentation rep = convertComponentModelToScimRepresentation(component);
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

    RealmModel realm = session.getContext().getRealm();

    ComponentModel existing = realm.getComponent(organization.getId());
    if (existing != null) {
      return Response.status(Response.Status.CONFLICT)
          .entity("SCIM configuration already exists for this organization")
          .build();
    }

    ComponentModel model =
        convertScimRepresentationToComponentModel(rep, organization.getId());
    model.setParentId(realm.getId());
    realm.addComponentModel(model);

    OrganizationScimRepresentation created =
        convertComponentModelToScimRepresentation(realm.getComponent(organization.getId()));

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.CREATE)
        .resourcePath(session.getContext().getUri())
        .representation(created)
        .success();

    return Response.status(Response.Status.CREATED).entity(created).build();
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

    RealmModel realm = session.getContext().getRealm();
    ComponentModel existing = realm.getComponent(organization.getId());
    if (existing == null) {
      throw new NotFoundException("No SCIM configuration found for organization");
    }

    updateComponentModelFromScimRepresentation(existing, rep);
    realm.updateComponent(existing);

    OrganizationScimRepresentation updated =
        convertComponentModelToScimRepresentation(realm.getComponent(organization.getId()));

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.UPDATE)
        .resourcePath(session.getContext().getUri())
        .representation(updated)
        .success();

    return Response.ok(updated).build();
  }

  @DELETE
  @Path("")
  public Response deleteScimConfig() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to manage SCIM config for %s", organization.getId()));
    }

    RealmModel realm = session.getContext().getRealm();
    ComponentModel existing = realm.getComponent(organization.getId());
    if (existing == null) {
      throw new NotFoundException("No SCIM configuration found for organization");
    }

    realm.removeComponent(existing);

    adminEvent
        .resource(ORGANIZATION.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri())
        .representation(organization.getId())
        .success();

    return Response.noContent().build();
  }
}
