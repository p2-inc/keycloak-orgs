package io.phasetwo.service.util;

import static io.phasetwo.service.Orgs.ORG_OWNER_CONFIG_KEY;
import static io.phasetwo.service.Orgs.ORG_SHARED_IDP_KEY;

import io.phasetwo.service.model.OrganizationModel;
import java.util.*;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.IdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.IdentityProviderEntity;
import org.keycloak.representations.idm.IdentityProviderRepresentation;

public final class IdentityProviders {

  private IdentityProviders() {}

  public static Set<String> getAttributeMultivalued(Map<String, String> config, String attrKey) {
    if (config == null) {
      return new HashSet<>();
    }
    String attrValue = config.get(attrKey);
    if (attrValue == null) return new HashSet<>();
    return new HashSet<>(Arrays.asList(Constants.CFG_DELIMITER_PATTERN.split(attrValue)));
  }

  public static void setAttributeMultivalued(
      Map<String, String> config, String attrKey, Set<String> attrValues) {
    if (config == null) {
      return;
    }

    if (attrValues == null || attrValues.size() == 0) {
      // Remove attribute
      config.put(attrKey, null);
    } else {
      String attrValueFull = String.join(Constants.CFG_DELIMITER, attrValues);
      config.put(attrKey, attrValueFull);
    }
  }

  public static void removeOrganization(String orgId, IdentityProviderModel idp) {
    var orgs = IdentityProviders.getAttributeMultivalued(idp.getConfig(), ORG_OWNER_CONFIG_KEY);
    orgs.remove(orgId);
    IdentityProviders.setAttributeMultivalued(idp.getConfig(), ORG_OWNER_CONFIG_KEY, orgs);
    if (orgs.size() > 1) {
      idp.getConfig().put(ORG_SHARED_IDP_KEY, "true");
    } else {
      idp.getConfig().put(ORG_SHARED_IDP_KEY, "false");
    }
  }

  public static void addMultiOrganization(
      OrganizationModel organization, IdentityProviderRepresentation representation) {
    var orgs =
        IdentityProviders.getAttributeMultivalued(representation.getConfig(), ORG_OWNER_CONFIG_KEY);
    orgs.add(organization.getId());
    IdentityProviders.setAttributeMultivalued(
        representation.getConfig(), ORG_OWNER_CONFIG_KEY, orgs);
    if (orgs.size() > 1) {
      representation.getConfig().put(ORG_SHARED_IDP_KEY, "true");
    } else {
      representation.getConfig().put(ORG_SHARED_IDP_KEY, "false");
    }
  }

  /* code copied from keycloak */
  public static IdentityProviderModel toModel(
      IdentityProviderEntity entity, KeycloakSession session) {
    if (entity == null) {
      return null;
    } else {
      IdentityProviderModel identityProviderModel =
          getModelFromProviderFactory(entity.getProviderId(), session);
      identityProviderModel.setProviderId(entity.getProviderId());
      identityProviderModel.setAlias(entity.getAlias());
      identityProviderModel.setDisplayName(entity.getDisplayName());
      identityProviderModel.setInternalId(entity.getInternalId());
      Map<String, String> config = new HashMap(entity.getConfig());
      identityProviderModel.setConfig(config);
      identityProviderModel.setEnabled(entity.isEnabled());
      identityProviderModel.setLinkOnly(entity.isLinkOnly());
      identityProviderModel.setHideOnLogin(entity.isHideOnLogin());
      identityProviderModel.setTrustEmail(entity.isTrustEmail());
      identityProviderModel.setAuthenticateByDefault(entity.isAuthenticateByDefault());
      identityProviderModel.setFirstBrokerLoginFlowId(entity.getFirstBrokerLoginFlowId());
      identityProviderModel.setPostBrokerLoginFlowId(entity.getPostBrokerLoginFlowId());
      identityProviderModel.setOrganizationId(entity.getOrganizationId());
      identityProviderModel.setStoreToken(entity.isStoreToken());
      identityProviderModel.setAddReadTokenRoleOnCreate(entity.isAddReadTokenRoleOnCreate());
      return identityProviderModel;
    }
  }

  /* code copied from keycloak */
  private static IdentityProviderModel getModelFromProviderFactory(
      String providerId, KeycloakSession session) {
    IdentityProviderFactory factory =
        (IdentityProviderFactory)
            session
                .getKeycloakSessionFactory()
                .getProviderFactory(IdentityProvider.class, providerId);
    if (factory == null) {
      factory =
          (IdentityProviderFactory)
              session
                  .getKeycloakSessionFactory()
                  .getProviderFactory(SocialIdentityProvider.class, providerId);
    }

    if (factory != null) {
      return factory.createConfig();
    } else {
      return new IdentityProviderModel();
    }
  }
}
