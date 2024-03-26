package io.phasetwo.service.datastore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.phasetwo.service.datastore.representation.KeycloakOrgsRealmRepresentation;
import io.phasetwo.service.datastore.representation.OrganizationAttributes;
import io.phasetwo.service.datastore.representation.OrganizationRepresentation;
import io.phasetwo.service.datastore.representation.OrganizationRoleRepresentation;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.resource.Converters;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.exportimport.ExportAdapter;
import org.keycloak.exportimport.ExportOptions;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.storage.ImportRealmFromRepresentationEvent;
import org.keycloak.storage.datastore.DefaultExportImportManager;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static org.keycloak.models.utils.StripSecretsUtils.stripForExport;

@JBossLog
public class KeycloakOrgsExportImportManager extends DefaultExportImportManager {
  private final KeycloakSession session;
  private final AdminAuth auth;

  public KeycloakOrgsExportImportManager(KeycloakSession session) {
    super(session);
    this.session = session;
    this.auth = authenticateRealmAdminRequest(session.getContext().getRequestHeaders());
  }

  @Override
  public void exportRealm(RealmModel realm, ExportOptions options, ExportAdapter callback) {
    callback.setType(MediaType.APPLICATION_JSON);
    callback.writeToOutputStream(
        outputStream -> {
          RealmRepresentation realmRepresentation =
              ExportUtils.exportRealm(session, realm, options, false);
          OrganizationProvider organizationProvider =
              session.getProvider(OrganizationProvider.class);

          KeycloakOrgsRealmRepresentation keycloakOrgsRepresentation;
          var mapper = new ObjectMapper();
          try {
            var json = mapper.writeValueAsString(realmRepresentation);
            log.debugv("export realm json: {0}", json);
            keycloakOrgsRepresentation =
                mapper.readValue(json, KeycloakOrgsRealmRepresentation.class);
          } catch (JsonProcessingException e) {
            throw new ModelException("unable to read contents from Json", e);
          }

          var organizations =
              organizationProvider
                  .searchForOrganizationStream(
                      realm, Map.of(), 0, Constants.DEFAULT_MAX_RESULTS, Optional.empty())
                  .map(
                      organization ->
                          Converters.convertOrganizationModelToOrganizationRepresentation(
                              organization, realm, options))
                  .toList();
          keycloakOrgsRepresentation.setOrganizations(organizations);

          stripForExport(session, keycloakOrgsRepresentation);

          JsonSerialization.writeValueToStream(outputStream, keycloakOrgsRepresentation);
          outputStream.close();
        });
  }

  @Override
  public RealmModel importRealm(InputStream requestBody) {
    KeycloakOrgsRealmRepresentation keycloakOrgsRepresentation;
    try {
      keycloakOrgsRepresentation =
          JsonSerialization.readValue(requestBody, KeycloakOrgsRealmRepresentation.class);
    } catch (IOException e) {
      throw new ModelException("unable to read contents from stream", e);
    }
    log.debugv("importRealm: {0}", keycloakOrgsRepresentation.getRealm());
    return ImportRealmFromRepresentationEvent.fire(session, keycloakOrgsRepresentation);
  }

  @Override
  public void importRealm(RealmRepresentation rep, RealmModel newRealm, boolean skipUserDependent) {
    OrganizationProvider organizationProvider = session.getProvider(OrganizationProvider.class);
    super.importRealm(rep, newRealm, skipUserDependent);

    var keycloakOrgsRepresentation = (KeycloakOrgsRealmRepresentation) rep;
    var organizations = keycloakOrgsRepresentation.getOrganizations();
    if (!CollectionUtil.isEmpty(organizations)) {
      organizations.forEach(
          organizationRepresentation -> {
            var org =
                createOrganization(
                    newRealm, organizationRepresentation.getOrganization(), organizationProvider);

            createOrganizationRoles(organizationRepresentation.getRoles(), org);

            createOrganizationIdp(newRealm, organizationRepresentation.getIdpLink(), org);

            addMembers(newRealm, organizationRepresentation, org);

            addInvitations(newRealm, organizationRepresentation, org);
          });
    }
  }

  private OrganizationModel createOrganization(
      RealmModel newRealm,
      OrganizationAttributes organizationAttributes,
      OrganizationProvider organizationProvider) {
    var org =
        organizationProvider.createOrganization(
            newRealm, organizationAttributes.getName(), this.auth.getUser(), false);
    org.setDisplayName(organizationAttributes.getDisplayName());
    org.setUrl(organizationAttributes.getUrl());
    if (organizationAttributes.getAttributes() != null) {
      organizationAttributes.getAttributes().forEach(org::setAttribute);
    }
    if (organizationAttributes.getDomains() != null) {
      org.setDomains(organizationAttributes.getDomains());
    }
    return org;
  }

  private void addInvitations(
      RealmModel newRealm,
      OrganizationRepresentation organizationRepresentation,
      OrganizationModel org) {
    organizationRepresentation
        .getInvitations()
        .forEach(
            invitation -> {
              var user =
                  KeycloakModelUtils.findUserByNameOrEmail(
                      session, newRealm, invitation.getEmail());
              if (user != null && org.hasMembership(user)) {
                throw new ModelException(
                    "User with email %s s already a member of organization: %s "
                        .formatted(invitation.getEmail(), org.getName()));
              }

              var inviter =
                  session.users().getUserByUsername(newRealm, invitation.getInviterUsername());
              if (inviter != null && org.hasMembership(inviter)) {
                InvitationModel i = org.addInvitation(invitation.getEmail(), inviter);
                i.setUrl(invitation.getRedirectUri());
                if (invitation.getRoles() != null) {
                  i.setRoles(invitation.getRoles());
                }
                if (invitation.getAttributes() != null && !invitation.getAttributes().isEmpty()) {
                  invitation.getAttributes().forEach(i::setAttribute);
                }
              } else {
                throw new ModelException(
                    "No inviter user with username %s in organization: %s"
                        .formatted(invitation.getInviterUsername(), org.getName()));
              }
            });
  }

  private void addMembers(
      RealmModel newRealm,
      OrganizationRepresentation organizationRepresentation,
      OrganizationModel org) {
    organizationRepresentation
        .getMembers()
        .forEach(
            member -> {
              var userModel = session.users().getUserByUsername(newRealm, member.getUsername());
              if (Objects.nonNull(userModel)) {
                org.grantMembership(userModel);
                member.getRoles().stream()
                    .map(org::getRoleByName)
                    .forEach(organizationRoleModel -> organizationRoleModel.grantRole(userModel));
              } else {
                throw new ModelException("No user with username: " + member.getUsername());
              }
            });
  }

  private static void createOrganizationIdp(
      RealmModel newRealm, String idpLink, OrganizationModel org) {
    if (Objects.nonNull(idpLink)) {
      IdentityProviderModel idp = newRealm.getIdentityProviderByAlias(idpLink);
      idp.getConfig().put(ORG_OWNER_CONFIG_KEY, org.getId());
      newRealm.updateIdentityProvider(idp);
    }
  }

  private static void createOrganizationRoles(
      List<OrganizationRoleRepresentation> roles, OrganizationModel org) {
    roles.stream()
        .filter(
            organizationRole ->
                Arrays.stream(OrganizationAdminAuth.DEFAULT_ORG_ROLES)
                    .noneMatch(role -> role.equals(organizationRole.getName())))
        .forEach(
            organizationRole -> {
              var role = org.addRole(organizationRole.getName());
              role.setDescription(organizationRole.getDescription());
            });
  }

  protected AdminAuth authenticateRealmAdminRequest(HttpHeaders headers) {
    String tokenString = AppAuthManager.extractAuthorizationHeaderToken(headers);
    if (tokenString == null) throw new NotAuthorizedException("Bearer");
    AccessToken token;
    try {
      JWSInput input = new JWSInput(tokenString);
      token = input.readJsonContent(AccessToken.class);
    } catch (JWSInputException e) {
      throw new NotAuthorizedException("Bearer token format error");
    }
    String realmName = token.getIssuer().substring(token.getIssuer().lastIndexOf('/') + 1);
    RealmManager realmManager = new RealmManager(session);
    RealmModel realm = realmManager.getRealmByName(realmName);
    if (realm == null) {
      throw new NotAuthorizedException("Unknown realm in token");
    }
    session.getContext().setRealm(realm);

    AuthenticationManager.AuthResult authResult =
        new AppAuthManager.BearerTokenAuthenticator(session)
            .setRealm(realm)
            .setConnection(session.getContext().getConnection())
            .setHeaders(headers)
            .authenticate();

    if (authResult == null) {
      log.debug("Token not valid");
      throw new NotAuthorizedException("Bearer");
    }

    return new AdminAuth(
        realm, authResult.getToken(), authResult.getUser(), authResult.getClient());
  }
}
