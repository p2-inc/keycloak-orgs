package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;
import static org.keycloak.models.utils.ModelToRepresentation.*;

import io.phasetwo.service.model.OrganizationModel;
import java.util.stream.Stream;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.UserModel;
import org.keycloak.representations.idm.UserRepresentation;

@JBossLog
public class MembersResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public MembersResource(OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<UserRepresentation> getMembers(
      @QueryParam("first") Integer firstResult, @QueryParam("max") Integer maxResults) {
    log.debugf("Get members for %s %s", realm.getName(), organization.getId());
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
    return organization
        .getMembersStream()
        .skip(firstResult)
        .limit(maxResults)
        .map(m -> toRepresentation(session, realm, m));
  }

  @DELETE
  @Path("{userId}")
  public Response removeMember(@PathParam("userId") String userId) {
    canDelete(userId);

    log.debugf("Remove member %s from %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (member
        .getUsername()
        .equals(OrganizationResourceProviderFactory.getDefaultAdminUsername(organization))) {
      throw new ForbiddenException("Cannot remove default organization user.");
    }
    if (member != null && organization.hasMembership(member)) {
      organization.revokeMembership(member);
      adminEvent
          .resource(ORGANIZATION_MEMBERSHIP.name())
          .operation(OperationType.DELETE)
          .resourcePath(session.getContext().getUri())
          .representation(userId)
          .success();
      return Response.noContent().build();
    } else {
      throw new NotFoundException();
    }
  }

  @GET
  @Path("{userId}")
  public Response getMember(@PathParam("userId") String userId) {
    log.debugf("Check membership %s for %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (member != null && organization.hasMembership(member)) {
      return Response.noContent().build();
    } else {
      throw new NotFoundException();
    }
  }

  @PUT
  @Path("{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response addMember(@PathParam("userId") String userId) {
    log.debugf("Add %s as member for %s %s", userId, realm.getName(), organization.getId());
    canManage();

    UserModel member = session.users().getUserById(realm, userId);
    if (member != null) {
      if (!organization.hasMembership(member)) {
        organization.grantMembership(member);
        adminEvent
            .resource(ORGANIZATION_MEMBERSHIP.name())
            .operation(OperationType.CREATE)
            .resourcePath(session.getContext().getUri())
            .representation(userId)
            .success();
      }
      return Response.created(session.getContext().getUri().getAbsolutePathBuilder().build())
          .build();
    } else {
      throw new NotFoundException();
    }
  }

  private void canManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageMembers(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have permission to manage members in org %s",
              auth.getUser().getId(), organization.getName()));
    }
  }

  private void canDelete(String userId) {
    var seppuku = (userId == getUser().getId());

    if (!seppuku) {
      canManage();
    }
  }
}
