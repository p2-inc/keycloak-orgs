package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;
import static org.keycloak.models.utils.ModelToRepresentation.*;

import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationModel;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.stream.Stream;
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
      @QueryParam("search") String searchQuery,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults) {
    log.debugf("Get members for %s %s [%s]", realm.getName(), organization.getId(), searchQuery);
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;
    return organization
        .searchForMembersStream(searchQuery, firstResult, maxResults)
        .map(m -> toRepresentation(session, realm, m));
  }

  @GET
  @Path("count")
  @Produces(MediaType.APPLICATION_JSON)
  public Long getMembersCount() {
    log.debugf("Get members count for %s %s", realm.getName(), organization.getId());
    return organization.getMembersCount();
  }

  @DELETE
  @Path("{userId}")
  public Response removeMember(@PathParam("userId") String userId) {
    canDelete(userId);

    log.debugf("Remove member %s from %s %s", userId, realm.getName(), organization.getId());
    UserModel member = session.users().getUserById(realm, userId);
    if (!Strings.isNullOrEmpty(member.getUsername())
        && member
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
    if (!userId.equals(user.getId())) {
      canManage();
    }
  }
}
