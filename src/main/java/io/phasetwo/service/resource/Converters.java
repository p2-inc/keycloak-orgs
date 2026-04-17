package io.phasetwo.service.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.metatavu.keycloak.scim.server.organization.ComponentScimConfig;
import fi.metatavu.keycloak.scim.server.organization.OrganizationScimConfig;
import io.phasetwo.service.model.InvitationModel;
import io.phasetwo.service.model.OrganizationModel;
import io.phasetwo.service.model.OrganizationRoleModel;
import io.phasetwo.service.model.jpa.entity.InvitationEntity;
import io.phasetwo.service.representation.*;
import java.util.List;
import java.util.Map;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.representations.account.UserRepresentation;

/** Utilities for converting Entities to/from Representations. */
public class Converters {

  public static OrganizationRole convertOrganizationRole(OrganizationRoleModel m) {
    OrganizationRole r =
        new OrganizationRole().id(m.getId()).name(m.getName()).description(m.getDescription());
    return r;
  }

  public static Organization convertOrganizationModelToOrganization(OrganizationModel e) {
    Organization o =
        new Organization()
            .id(e.getId())
            .name(e.getName())
            .displayName(e.getDisplayName())
            .domains(e.getDomains())
            .url(e.getUrl())
            .realm(e.getRealm().getName());
    o.setAttributes(e.getAttributes());
    return o;
  }

  public static UserRepresentation convertUserEntityToUserRepresentation(UserEntity e) {
    UserRepresentation r = new UserRepresentation();
    r.setEmail(e.getEmail());
    r.setFirstName(e.getFirstName());
    r.setLastName(e.getLastName());
    r.setUsername(e.getUsername());
    r.setEmailVerified(e.isEmailVerified());
    r.setId(e.getId());
    Map<String, List<String>> attr = Maps.newHashMap();
    e.getAttributes()
        .forEach(
            a -> {
              List<String> l = attr.get(a.getName());
              if (l == null) l = Lists.newArrayList();
              if (!l.contains(a.getValue())) l.add(a.getValue());
              attr.put(a.getName(), l);
            });
    r.setAttributes(attr);
    return r;
  }

  public static Invitation convertInvitationEntityToInvitation(InvitationEntity e) {
    Invitation i =
        new Invitation()
            .id(e.getId())
            .email(e.getEmail())
            .createdAt(e.getCreatedAt())
            .inviterId(e.getInviterId())
            .organizationId(e.getOrganization().getId())
            .roles(Lists.newArrayList(e.getRoles()));
    Map<String, List<String>> attr = Maps.newHashMap();
    e.getAttributes()
        .forEach(
            a -> {
              List<String> l = attr.get(a.getName());
              if (l == null) l = Lists.newArrayList();
              if (!l.contains(a.getValue())) l.add(a.getValue());
              attr.put(a.getName(), l);
            });
    i.setAttributes(attr);
    return i;
  }

  public static OrganizationScimRepresentation convertComponentModelToScimRepresentation(
      ComponentModel component) {
    ComponentScimConfig config = new ComponentScimConfig(component);
    OrganizationScimRepresentation rep = new OrganizationScimRepresentation();
    rep.setEnabled(config.isEnabled());
    rep.setEmailAsUsername(config.getEmailAsUsername());
    rep.setLinkIdp(config.getLinkIdp());

    var mode = config.getAuthenticationMode();
    if (mode != null) {
      switch (mode) {
        case KEYCLOAK:
          rep.setAuth(new KeycloakScimAuth());
          break;
        case EXTERNAL:
          String basicUser = config.getBasicAuthUsername();
          String basicPass = config.getBasicAuthPassword();
          String secret = config.getSharedSecret();

          if (basicUser != null && !basicUser.isBlank()) {
            BasicAuthScimAuth basicAuth = new BasicAuthScimAuth();
            basicAuth.setUsername(basicUser);
            basicAuth.setPassword(basicPass);
            rep.setAuth(basicAuth);
          } else if (secret != null && !secret.isBlank()) {
            SharedSecretScimAuth secretAuth = new SharedSecretScimAuth();
            secretAuth.setSharedSecret(secret);
            rep.setAuth(secretAuth);
          } else {
            JwtScimAuth jwtAuth = new JwtScimAuth();
            jwtAuth.setIssuer(config.getExternalIssuer());
            jwtAuth.setAudience(config.getExternalAudience());
            jwtAuth.setJwksUri(config.getExternalJwksUri());
            rep.setAuth(jwtAuth);
          }
          break;
      }
    }
    return rep;
  }

  public static ComponentModel convertScimRepresentationToComponentModel(
      OrganizationScimRepresentation rep, String organizationId) {
    ComponentModel model = new ComponentModel();
    model.setId(organizationId);
    model.setName("Organization SCIM");
    model.setProviderId("Organization SCIM");
    model.setProviderType("org.keycloak.storage.UserStorageProvider");
    model.put(ComponentScimConfig.ORGANIZATION_ID, organizationId);

    if (rep.getEnabled() != null) {
      model.put(ComponentScimConfig.ENABLED_PROPERTY, rep.getEnabled().toString());
    }
    if (rep.getEmailAsUsername() != null) {
      model.put(
          OrganizationScimConfig.SCIM_EMAIL_AS_USERNAME, rep.getEmailAsUsername().toString());
    }
    if (rep.getLinkIdp() != null) {
      model.put(OrganizationScimConfig.SCIM_LINK_IDP, rep.getLinkIdp().toString());
    }

    OrganizationScimAuth auth = rep.getAuth();
    if (auth instanceof KeycloakScimAuth) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "KEYCLOAK");
    } else if (auth instanceof JwtScimAuth jwt) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "EXTERNAL");
      if (jwt.getIssuer() != null) {
        model.put(OrganizationScimConfig.SCIM_EXTERNAL_ISSUER, jwt.getIssuer());
      }
      if (jwt.getAudience() != null) {
        model.put(OrganizationScimConfig.SCIM_EXTERNAL_AUDIENCE, jwt.getAudience());
      }
      if (jwt.getJwksUri() != null) {
        model.put(OrganizationScimConfig.SCIM_EXTERNAL_JWKS_URI, jwt.getJwksUri());
      }
    } else if (auth instanceof SharedSecretScimAuth secret) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "EXTERNAL");
      if (secret.getSharedSecret() != null) {
        model.put(OrganizationScimConfig.SCIM_EXTERNAL_SHARED_SECRET, secret.getSharedSecret());
      }
    } else if (auth instanceof BasicAuthScimAuth basic) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "EXTERNAL");
      if (basic.getUsername() != null) {
        model.put(OrganizationScimConfig.SCIM_BASIC_AUTH_USERNAME, basic.getUsername());
      }
      if (basic.getPassword() != null) {
        model.put(OrganizationScimConfig.SCIM_BASIC_AUTH_PASSWORD, basic.getPassword());
      }
    }

    return model;
  }

  public static void updateComponentModelFromScimRepresentation(
      ComponentModel model, OrganizationScimRepresentation rep) {
    if (rep.getEnabled() != null) {
      model.put(ComponentScimConfig.ENABLED_PROPERTY, rep.getEnabled().toString());
    }
    if (rep.getEmailAsUsername() != null) {
      model.put(
          OrganizationScimConfig.SCIM_EMAIL_AS_USERNAME, rep.getEmailAsUsername().toString());
    }
    if (rep.getLinkIdp() != null) {
      model.put(OrganizationScimConfig.SCIM_LINK_IDP, rep.getLinkIdp().toString());
    }

    OrganizationScimAuth auth = rep.getAuth();
    if (auth instanceof KeycloakScimAuth) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "KEYCLOAK");
      // Clear external fields
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_ISSUER);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_AUDIENCE);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_JWKS_URI);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_SHARED_SECRET);
      model.getConfig().remove(OrganizationScimConfig.SCIM_BASIC_AUTH_USERNAME);
      model.getConfig().remove(OrganizationScimConfig.SCIM_BASIC_AUTH_PASSWORD);
    } else if (auth instanceof JwtScimAuth jwt) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "EXTERNAL");
      model.put(OrganizationScimConfig.SCIM_EXTERNAL_ISSUER, jwt.getIssuer());
      model.put(OrganizationScimConfig.SCIM_EXTERNAL_AUDIENCE, jwt.getAudience());
      model.put(OrganizationScimConfig.SCIM_EXTERNAL_JWKS_URI, jwt.getJwksUri());
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_SHARED_SECRET);
      model.getConfig().remove(OrganizationScimConfig.SCIM_BASIC_AUTH_USERNAME);
      model.getConfig().remove(OrganizationScimConfig.SCIM_BASIC_AUTH_PASSWORD);
    } else if (auth instanceof SharedSecretScimAuth secret) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "EXTERNAL");
      model.put(OrganizationScimConfig.SCIM_EXTERNAL_SHARED_SECRET, secret.getSharedSecret());
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_ISSUER);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_AUDIENCE);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_JWKS_URI);
      model.getConfig().remove(OrganizationScimConfig.SCIM_BASIC_AUTH_USERNAME);
      model.getConfig().remove(OrganizationScimConfig.SCIM_BASIC_AUTH_PASSWORD);
    } else if (auth instanceof BasicAuthScimAuth basic) {
      model.put(OrganizationScimConfig.SCIM_AUTHENTICATION_MODE, "EXTERNAL");
      model.put(OrganizationScimConfig.SCIM_BASIC_AUTH_USERNAME, basic.getUsername());
      model.put(OrganizationScimConfig.SCIM_BASIC_AUTH_PASSWORD, basic.getPassword());
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_ISSUER);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_AUDIENCE);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_JWKS_URI);
      model.getConfig().remove(OrganizationScimConfig.SCIM_EXTERNAL_SHARED_SECRET);
    }
  }

  public static Invitation convertInvitationModelToInvitation(InvitationModel e) {
    Invitation i =
        new Invitation()
            .id(e.getId())
            .email(e.getEmail())
            .createdAt(e.getCreatedAt())
            .inviterId(e.getInviter() != null ? e.getInviter().getId() : null)
            .invitationUrl(e.getUrl())
            .organizationId(e.getOrganization().getId())
            .roles(Lists.newArrayList(e.getRoles()));
    i.setAttributes(Maps.newHashMap(e.getAttributes()));
    return i;
  }
}
