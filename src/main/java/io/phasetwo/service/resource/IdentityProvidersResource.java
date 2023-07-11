package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.*;
import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import io.phasetwo.service.model.OrganizationModel;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.StripSecretsUtils;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.TestLdapConnectionRepresentation;
import org.keycloak.services.managers.LDAPServerCapabilitiesManager;

@JBossLog
public class IdentityProvidersResource extends OrganizationAdminResource {

  private final OrganizationModel organization;

  public IdentityProvidersResource(
      OrganizationAdminResource parent, OrganizationModel organization) {
    super(parent);
    this.organization = organization;
  }

  @Path("{alias}")
  public IdentityProviderResource identityProvider(@PathParam("alias") String alias) {
    org.keycloak.services.resources.admin.IdentityProviderResource kcResource =
        getIdpResource().getIdentityProvider(alias);
    IdentityProviderRepresentation provider = kcResource.getIdentityProvider();
    if (!(provider.getConfig().containsKey(ORG_OWNER_CONFIG_KEY)
        && organization.getId().equals(provider.getConfig().get(ORG_OWNER_CONFIG_KEY)))) {
      throw new NotFoundException(String.format("%s not found", alias));
    }
    return new IdentityProviderResource(this, organization, alias, kcResource);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<IdentityProviderRepresentation> getIdentityProviders() {
    return realm
        .getIdentityProvidersStream()
        .filter(provider -> idpInOrg(provider))
        .map(
            provider ->
                StripSecretsUtils.strip(ModelToRepresentation.toRepresentation(realm, provider)));
  }

  public static void idpDefaults(
      OrganizationModel organization, IdentityProviderRepresentation representation) {
    // defaults? overrides?
    representation.getConfig().put("syncMode", "FORCE");
    representation.getConfig().put("hideOnLoginPage", "true");
    representation.getConfig().put(ORG_OWNER_CONFIG_KEY, organization.getId());
    representation.setPostBrokerLoginFlowAlias(ORG_AUTH_FLOW_ALIAS);
    //  - firstBrokerLoginFlowAlias
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createIdentityProvider(IdentityProviderRepresentation representation) {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to create identity providers for %s", organization.getId()));
    }

    // Override alias to prevent collisions
    // representation.setAlias(KeycloakModelUtils.generateId());

    idpDefaults(organization, representation);

    // Organization can have only one active idp
    // Activating an idp deactivates all others
    if (representation.isEnabled()) {
      realm
          .getIdentityProvidersStream()
          .filter(provider -> idpInOrg(provider))
          .forEach(
              provider -> {
                provider.setEnabled(false);
                realm.updateIdentityProvider(provider); // weird that this is necessary
              });
    }

    Response resp = getIdpResource().create(representation);
    if (resp.getStatus() == Response.Status.CREATED.getStatusCode()) {
      return Response.created(
              session
                  .getContext()
                  .getUri()
                  .getAbsolutePathBuilder()
                  .path(representation.getAlias())
                  .build())
          .build();
    } else {
      return resp;
    }
  }

  @POST
  @Path("import-config")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> importConfig(Map<String, Object> data) throws IOException {
    return getIdpResource().importFrom(data);
  }

  @POST
  @Path("import-config")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> importConfig() throws IOException {
    return getIdpResource().importFrom();
  }

  private boolean idpInOrg(IdentityProviderModel provider) {
    return (provider.getConfig().containsKey(ORG_OWNER_CONFIG_KEY)
        && organization.getId().equals(provider.getConfig().get(ORG_OWNER_CONFIG_KEY)));
  }

  private org.keycloak.services.resources.admin.IdentityProvidersResource getIdpResource() {
    OrganizationAdminPermissionEvaluator authEval =
        new OrganizationAdminPermissionEvaluator(organization, auth, permissions);
    return new org.keycloak.services.resources.admin.IdentityProvidersResource(
        realm, session, authEval, adminEvent);
  }

  @Path("test-ldap-connection")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response testLDAPConnection(TestLdapConnectionRepresentation config) {
    try {
      LDAPServerCapabilitiesManager.testLDAP(config, session, realm);
      return Response.noContent().build();
    } catch (Exception e) {
      throw new BadRequestException("LDAP test error");
    }
  }

  @POST
  @Path("ldap")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(ComponentRepresentation rep) {
    // make a realm
    // set generic login theme
    // create a client for the core realm
    // create an idp in the core realm
    return null;
  }
}
