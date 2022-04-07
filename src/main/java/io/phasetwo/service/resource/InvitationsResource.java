package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.Invitation;
import io.phasetwo.service.representation.InvitationRequest;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.Valid;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.Constants;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminRoot;

@JBossLog
public class InvitationsResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public InvitationsResource(RealmModel realm, OrganizationModel organization) {
    super(realm);
    this.organization = organization;
  }

  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createInvitation(@Valid InvitationRequest invitation) {
    String email = invitation.getEmail();
    try {
      log.infof("Create invitation for %s %s %s", email, realm.getName(), organization.getId());
      canManage();

      if (email == null || !isValidEmail(email))
        throw new BadRequestException("Invalid email: " + email);
      Invitation o =
          convertInvitationModelToInvitation(organization.addInvitation(email, auth.getUser()));

      adminEvent
          .resource(INVITATION.name())
          .operation(OperationType.CREATE)
          .resourcePath(session.getContext().getUri(), o.getId())
          .representation(o)
          .success();

      // /auth/realms/:realm/orgs/:orgId/invtations/:name"
      URI location =
          AdminRoot.realmsUrl(session.getContext().getUri())
              .path(realm.getName())
              .path("orgs")
              .path(organization.getId())
              .path("invitations")
              .path(o.getId())
              .build();
      return Response.created(location).build();
    } catch (Exception e) {
      throw new InternalServerErrorException(e);
    }
  }

  @GET
  @Path("")
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<Invitation> listInvitations(
      @QueryParam("search") String searchQuery,
      @QueryParam("first") Integer firstResult,
      @QueryParam("max") Integer maxResults) {
    log.infof("Get invitations for %s %s", realm.getName(), organization.getId());
    Optional<String> search = Optional.ofNullable(searchQuery);
    firstResult = firstResult != null ? firstResult : 0;
    maxResults = maxResults != null ? maxResults : Constants.DEFAULT_MAX_RESULTS;

    return organization
        .getInvitationsStream()
        .filter(i -> !search.isPresent() || i.getEmail().contains(search.get()))
        .skip(firstResult)
        .limit(maxResults)
        .map(i -> convertInvitationModelToInvitation(i));
  }

  @DELETE
  @Path("{invitationId}")
  public Response removeInvitation(@PathParam("invitationId") String invitationId) {
    canManage();

    // inefficient
    InvitationModel invitation =
        organization
            .getInvitationsStream()
            .filter(i -> i.getId().equals(invitationId))
            .findAny()
            .orElse(null);
    if (invitation == null)
      throw new NotFoundException(String.format("No invitation with id %s", invitationId));

    organization.revokeInvitation(invitationId);

    adminEvent
        .resource(INVITATION.name())
        .operation(OperationType.DELETE)
        .resourcePath(session.getContext().getUri(), invitation.getId())
        .representation(convertInvitationModelToInvitation(invitation))
        .success();

    return Response.status(204).build();
  }

  private void canManage() {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageInvitations(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "User %s doesn't have permission to manage invitations in org %s",
              auth.getUser().getId(), organization.getName()));
    }
  }

  /** Get a validated email address */
  private static InternetAddress getValidEmail(String email) throws AddressException {
    Objects.requireNonNull(email, "email must not be null to validate");
    try {
      if (email.startsWith("mailto:")) email = email.substring(7);
      InternetAddress emailAddr = new InternetAddress(email);
      emailAddr.validate();
      return emailAddr;
    } catch (AddressException e) {
      throw e;
    }
  }

  private static boolean isValidEmail(String email) {
    try {
      getValidEmail(email);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
