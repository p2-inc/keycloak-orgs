package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;

@JBossLog
public class IdentityProviderMapperResource extends OrganizationAdminResource {

  private final String orgId;
  private final String alias;
  private final String id;

  public IdentityProviderMapperResource(RealmModel realm, String orgId, String alias, String id) {
    super(realm);
    this.orgId = orgId;
    this.alias = alias;
    this.id = id;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public IdentityProviderMapperRepresentation getIdentityProviderMapper() {
    return null;
  }

  @DELETE
  public Response delete() {
    return null;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  public Response update(IdentityProviderMapperRepresentation representation) {
    return null;
  }
}
