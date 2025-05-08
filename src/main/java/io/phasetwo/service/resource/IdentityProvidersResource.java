package io.phasetwo.service.resource;

import static io.phasetwo.service.Orgs.*;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.representation.LinkIdp;
import io.phasetwo.service.util.IdentityProviders;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    if (!canManageIdp()) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to manage identity provider %s for %s",
              alias, organization.getId()));
    }

    IdentityProviderRepresentation provider = kcResource.getIdentityProvider();
    var orgs =
        IdentityProviders.getAttributeMultivalued(provider.getConfig(), ORG_OWNER_CONFIG_KEY);
    if (!orgs.contains(organization.getId())) {
      throw new NotFoundException(String.format("%s not found", alias));
    }
    return new IdentityProviderResource(this, organization, alias, kcResource);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Stream<IdentityProviderRepresentation> getIdentityProviders() {
    // loads only idps solely owned by this org, not shared
    Stream<IdentityProviderModel> owned =
        session
            .identityProviders()
            .getAllStream(ImmutableMap.of(ORG_OWNER_CONFIG_KEY, organization.getId()), null, null);
    // loads all idps that are shared
    Stream<IdentityProviderModel> shared =
        session
            .identityProviders()
            .getAllStream(ImmutableMap.of(ORG_SHARED_IDP_KEY, "true"), null, null);

    return Stream.concat(owned, shared)
        .filter(provider -> canViewIdp())
        .filter(this::idpInOrg)
        .map(
                provider ->
                    StripSecretsUtils.stripSecrets(
                        session, ModelToRepresentation.toRepresentation(realm, provider)));
  }

  protected void idpDefaults(
      IdentityProviderRepresentation representation, Optional<LinkIdp> linkIdp) {
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
    representation.setHideOnLogin(true);
    representation.getConfig().put(IdentityProviderModel.SYNC_MODE, syncMode);
    representation.setPostBrokerLoginFlowAlias(postBrokerFlow);

    var isSharedIdpsConfigEnabled = realm.getAttribute(ORG_CONFIG_SHARED_IDPS_KEY, false);

    if (isSharedIdpsConfigEnabled) {
      IdentityProviders.addMultiOrganization(organization, representation);
    } else {
      representation.getConfig().put(ORG_SHARED_IDP_KEY, "false");
      IdentityProviders.setAttributeMultivalued(
          representation.getConfig(), ORG_OWNER_CONFIG_KEY, Set.of(organization.getId()));
    }
  }

  private void deactivateOtherIdps(
      IdentityProviderRepresentation representation,
      boolean unlink,
      boolean disable,
      String orgId) {
    if (representation.isEnabled()) {
      session
          .identityProviders()
          .getAllStream()
          .filter(this::idpInOrg)
          .forEach(
              provider -> {
                if (disable) provider.setEnabled(false);
                if (unlink) {
                  IdentityProviders.removeOrganization(orgId, provider);
                }
                session.identityProviders().update(provider); // weird that this is necessary
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

  private LinkIdp linkFromRep(IdentityProviderRepresentation representation) {
    LinkIdp link = new LinkIdp();
    link.setAlias(representation.getAlias());
    link.setPostBrokerFlow(representation.getPostBrokerLoginFlowAlias());
    Map<String, String> config = representation.getConfig();
    if (config != null) {
      link.setSyncMode(config.get(IdentityProviderModel.SYNC_MODE));
    }
    return link;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createIdentityProvider(IdentityProviderRepresentation representation) {
    if (!auth.hasManageOrgs() && !auth.hasOrgManageIdentityProviders(organization)) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to create identity providers for %s", organization.getId()));
    }

    idpDefaults(representation, Optional.of(linkFromRep(representation)));
    deactivateOtherIdps(representation, false, true, organization.getId());

    Response resp = getIdpResource().create(representation);
    if (resp.getStatus() == Response.Status.CREATED.getStatusCode()) {
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
    if (!auth.hasManageOrgs()) {
      throw new NotAuthorizedException(
          String.format(
              "Insufficient permission to link identity providers for %s", organization.getId()));
    }

    // get an idp with the same alias
    IdentityProviderModel idp = session.identityProviders().getByAlias(linkIdp.getAlias());
    if (idp == null) {
      throw new NotFoundException(String.format("No IdP found with alias %s", linkIdp.getAlias()));
    }
    if (!idp.isEnabled()) {
      throw new BadRequestException(
          String.format("Cannot link disabled IdP %s", linkIdp.getAlias()));
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

    deactivateOtherIdps(representation, true, false, organization.getId());

    try {
      IdentityProviderModel updated = RepresentationToModel.toModel(realm, representation, session);
      session.identityProviders().update(updated);
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
    var orgs =
        IdentityProviders.getAttributeMultivalued(provider.getConfig(), ORG_OWNER_CONFIG_KEY);
    return orgs.contains(organization.getId());
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

  private boolean canViewIdp() {
    var isSharedIdpsConfigEnabled = realm.getAttribute(ORG_CONFIG_SHARED_IDPS_KEY, false);

    if (!isSharedIdpsConfigEnabled) {
      return true;
    }

    return auth.hasOrgViewIdentityProviders(organization) || auth.hasViewOrgs();
  }

  private boolean canManageIdp() {
    var isSharedIdpsConfigEnabled = realm.getAttribute(ORG_CONFIG_SHARED_IDPS_KEY, false);

    if (!isSharedIdpsConfigEnabled) {
      return true;
    }

    return auth.hasManageOrgs() || auth.hasOrgManageIdentityProviders(organization);
  }
}
