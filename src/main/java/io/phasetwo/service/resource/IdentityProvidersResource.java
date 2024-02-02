package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.*;
import static io.phasetwo.service.resource.Converters.*;
import static io.phasetwo.service.resource.OrganizationResourceType.*;

import com.google.common.base.Strings;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.LinkIdp;
import jakarta.validation.constraints.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
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

  protected void idpDefaults(
      IdentityProviderRepresentation representation, Optional<LinkIdp> linkIdp) {
    // defaults? overrides?
    String syncMode =
        linkIdp
            .map(LinkIdp::getSyncMode)
            .orElse(
                Optional.ofNullable(realm.getAttribute(ORG_DEFAULT_SYNC_MODE_KEY)).orElse("FORCE"));
    String postBrokerFlow =
        linkIdp
            .map(LinkIdp::getPostBrokerFlow)
            .orElse(
                Optional.ofNullable(realm.getAttribute(ORG_DEFAULT_POST_BROKER_FLOW_KEY))
                    .orElse(ORG_AUTH_FLOW_ALIAS));
    log.debugf(
        "using syncMode %s, postBrokerFlow %s for idp %s",
        syncMode, postBrokerFlow, representation.getAlias());

    representation.getConfig().put("syncMode", syncMode);
    representation.getConfig().put("hideOnLoginPage", "true");
    representation.getConfig().put(ORG_OWNER_CONFIG_KEY, organization.getId());
    representation.setPostBrokerLoginFlowAlias(postBrokerFlow);
    // TODO firstBrokerLoginFlowAlias
  }

  private void deactivateOtherIdps(IdentityProviderRepresentation representation) {
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
  }

  private Response createdResponse(IdentityProviderRepresentation representation) {
    return Response.created(
            session
                .getContext()
                .getUri()
                .getAbsolutePathBuilder()
                .path(representation.getAlias())
                .build())
        .build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createIdentityProvider(IdentityProviderRepresentation representation) {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to create identity providers for %s", organization.getId()));
    }

    idpDefaults(representation, Optional.empty());

    Response resp = getIdpResource().create(representation);
    if (resp.getStatus() == Response.Status.CREATED.getStatusCode()) {
      deactivateOtherIdps(representation);
      return createdResponse(representation);
    } else {
      return resp;
    }
  }

  @POST
  @Path("link")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response linkIdp(LinkIdp linkIdp) {
    // authz
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to create identity providers for %s", organization.getId()));
    }

    // get an idp with the same alias
    IdentityProviderModel idp = realm.getIdentityProviderByAlias(linkIdp.getAlias());
    if (idp == null) {
      throw new NotFoundException(String.format("No IdP found with alias %s", linkIdp.getAlias()));
    }

    // does it already contain an orgId for a different org?
    String orgId = idp.getConfig().get(ORG_OWNER_CONFIG_KEY);
    if (orgId != null && !organization.getId().equals(orgId)) {
      throw new ClientErrorException(
          String.format("IdP with alias %s unavailable for linking.", linkIdp.getAlias()),
          Response.Status.CONFLICT);
    }

    IdentityProviderRepresentation representation =
        ModelToRepresentation.toRepresentation(realm, idp);
    idpDefaults(representation, Optional.of(linkIdp));
    if (!Strings.isNullOrEmpty(linkIdp.getSyncMode())) {
      representation.getConfig().put("syncMode", linkIdp.getSyncMode());
    }
    if (!Strings.isNullOrEmpty(linkIdp.getPostBrokerFlow())) {
      representation.setPostBrokerLoginFlowAlias(linkIdp.getPostBrokerFlow());
    }

    try {
      IdentityProviderModel updated = RepresentationToModel.toModel(realm, representation, session);
      realm.updateIdentityProvider(updated);
      deactivateOtherIdps(representation);
      return createdResponse(representation);
    } catch (Exception e) {
      throw new InternalServerErrorException(
          String.format("Error updating IdP %s", representation.getAlias()), e);
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
