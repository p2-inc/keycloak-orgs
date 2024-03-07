package io.phasetwo.service.auth.storage.datastore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.phasetwo.service.model.OrganizationProvider;
import io.phasetwo.service.representation.Organization;
import io.phasetwo.service.resource.Converters;
import io.phasetwo.service.resource.OrganizationAdminAuth;
import io.phasetwo.service.resource.OrganizationAdminPermissionEvaluator;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;
import org.keycloak.exportimport.ExportAdapter;
import org.keycloak.exportimport.ExportOptions;
import org.keycloak.exportimport.util.ExportUtils;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.IdentityProviderRepresentation;
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
import java.util.Map;
import java.util.Optional;

import static org.keycloak.models.utils.StripSecretsUtils.stripForExport;

/**
 * This wraps the functionality about export/import for the storage.
 *
 * @author Alexander Schwartz
 */
public class PhaseTwoExportImportManager extends DefaultExportImportManager {
    private final KeycloakSession session;
    private final AdminAuth auth;
    private static final Logger logger = Logger.getLogger(PhaseTwoExportImportManager.class);

    public PhaseTwoExportImportManager(KeycloakSession session) {
        super(session);
        this.session = session;
        this.auth = authenticateRealmAdminRequest(session.getContext().getRequestHeaders());
    }

    @Override
    public void exportRealm(RealmModel realm, ExportOptions options, ExportAdapter callback) {
        callback.setType(MediaType.APPLICATION_JSON);
        callback.writeToOutputStream(outputStream -> {
            RealmRepresentation realmRepresentation = ExportUtils.exportRealm(session, realm, options, false);
            OrganizationProvider organizationProvider = session.getProvider(OrganizationProvider.class);

            PhaseTwoRealmRepresentation phaseTwoRepresentation;
            var mapper = new ObjectMapper();
            try {
                var json = mapper.writeValueAsString(realmRepresentation);
                logger.debugv("export realm json: {0}", json);
                phaseTwoRepresentation = mapper.readValue(json, PhaseTwoRealmRepresentation.class);
            } catch (JsonProcessingException e) {
                throw new ModelException("unable to read contents from Json", e);
            }

            var organizations = organizationProvider.searchForOrganizationStream(
                            realm,
                            Map.of(),
                            0,
                            Constants.DEFAULT_MAX_RESULTS,
                            Optional.empty())
                    .map(Converters::convertOrganizationModelToOrganizationRepresentation)
                    .toList();
            phaseTwoRepresentation.setOrganizations(organizations);

            stripForExport(session, phaseTwoRepresentation);

            JsonSerialization.writeValueToStream(outputStream, phaseTwoRepresentation);
            outputStream.close();
        });
    }

    @Override
    public RealmModel importRealm(InputStream requestBody) {
        PhaseTwoRealmRepresentation phaseTwoRepresentation;
        try {
            phaseTwoRepresentation = JsonSerialization.readValue(requestBody, PhaseTwoRealmRepresentation.class);
        } catch (IOException e) {
            throw new ModelException("unable to read contents from stream", e);
        }
        logger.debugv("importRealm: {0}", phaseTwoRepresentation.getRealm());
        return ImportRealmFromRepresentationEvent.fire(session, phaseTwoRepresentation);
    }

    @Override
    public void importRealm(RealmRepresentation rep, RealmModel newRealm, boolean skipUserDependent) {
        OrganizationProvider organizationProvider = session.getProvider(OrganizationProvider.class);
        super.importRealm(rep, newRealm, skipUserDependent);

        var phaseTwoRepresentation = (PhaseTwoRealmRepresentation) rep;
        var organizations = phaseTwoRepresentation.getOrganizations();
        organizations
                .forEach(organizationRepresentation -> {
                    var organization = organizationRepresentation.getOrganization();
                    var org = organizationProvider.createOrganization(newRealm, organization.getName(), this.auth.getUser(), true);
                    org.setDisplayName(organization.getDisplayName());
                    org.setUrl(organization.getUrl());
                    if (organization.getAttributes() != null)
                        organization.getAttributes().forEach((k, v) -> org.setAttribute(k, v));
                    if (organization.getDomains() != null) org.setDomains(organization.getDomains());

                    organizationRepresentation
                            .getRoles()
                            .stream()
                            .filter(organizationRole ->
                                    Arrays.stream(OrganizationAdminAuth.DEFAULT_ORG_ROLES).noneMatch(role -> role.equals(organizationRole.getName())))
                            .forEach(organizationRole ->
                            {
                                var role = org.addRole(organizationRole.getName());
                                role.setDescription(organizationRole.getDescription());
                            });

//                    organizationRepresentation
//                            .getLinkIdps()
//                            .stream()
//                            .forEach(linkIdp -> {
//                                org.keycloak.services.resources.admin.IdentityProviderResource kcResource =
//                                        getIdpResource().getIdentityProvider(alias);
//                                IdentityProviderRepresentation provider = kcResource.getIdentityProvider();
//                            });
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

        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                .setConnection(session.getContext().getConnection())
                .setHeaders(headers)
                .authenticate();

        if (authResult == null) {
            logger.debug("Token not valid");
            throw new NotAuthorizedException("Bearer");
        }

        return new AdminAuth(realm, authResult.getToken(), authResult.getUser(), authResult.getClient());
    }
}
