package io.phasetwo.service.resource;

import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;
import static io.phasetwo.service.Orgs.*;

import io.phasetwo.service.model.OrganizationModel;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;
import javax.validation.constraints.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.StripSecretsUtils;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
import org.keycloak.representations.idm.TestLdapConnectionRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.LDAPServerCapabilitiesManager;
import org.keycloak.services.resources.admin.AdminRoot;

@JBossLog
public class IdentityProvidersResource extends OrganizationAdminResource {


  private final OrganizationModel organization;

  public IdentityProvidersResource(RealmModel realm, OrganizationModel organization) {
    super(realm);
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
    IdentityProviderResource resource =
        new IdentityProviderResource(realm, organization, alias, kcResource);
    ResteasyProviderFactory.getInstance().injectProperties(resource);
    resource.setup();
    return resource;
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
    representation.setPostBrokerLoginFlowAlias​(ORG_AUTH_FLOW_ALIAS);
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
      // /auth/realms/:realm/orgs/:orgId/idps/:alias"
      URI location =
          AdminRoot.realmsUrl(session.getContext().getUri())
              .path(realm.getName())
              .path("orgs")
              .path(organization.getId())
              .path("idps")
              .path(representation.getAlias())
              .build();
      return Response.created(location).build();
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
  public Map<String, String> importConfig(MultipartFormDataInput input) throws IOException {
    return getIdpResource().importFrom(input);
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
    boolean result = LDAPServerCapabilitiesManager.testLDAP(config, session, realm);
    return result
        ? Response.noContent().build()
        : ErrorResponse.error("LDAP test error", Response.Status.BAD_REQUEST);
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
