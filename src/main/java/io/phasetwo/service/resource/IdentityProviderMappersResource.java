package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import java.util.stream.Stream;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.IdentityProviderMapperRepresentation;

@JBossLog
public class IdentityProviderMappersResource extends OrganizationAdminResource {

  private final String orgId;
  private final String alias;

  public IdentityProviderMappersResource(RealmModel realm, String orgId, String alias) {
    super(realm);
    this.orgId = orgId;
    this.alias = alias;
  }

  @Path("{id}")
  public IdentityProviderMapperResource identityProviders(@PathParam("id") String id) {
    IdentityProviderMapperResource resource =
        new IdentityProviderMapperResource(realm, orgId, alias, id);
    ResteasyProviderFactory.getInstance().injectProperties(resource);
    resource.setup();
    return resource;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<IdentityProviderMapperRepresentation> getIdentityProviderMappers() {
    return null;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createIdentityProviderMapper(
      IdentityProviderMapperRepresentation representation) {
    return null;
  }
}
