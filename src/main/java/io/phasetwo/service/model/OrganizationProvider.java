package io.phasetwo.service.model;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.phasetwo.service.model.jpa.entity.ExtOrganizationEntity;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

public interface OrganizationProvider extends Provider {

  OrganizationModel createOrganization(
      RealmModel realm, String name, UserModel createdBy, boolean admin);

  OrganizationModel createOrganization(
      RealmModel realm, String id, String name, UserModel createdBy, boolean admin);

  OrganizationModel getOrganizationById(RealmModel realm, String id);

  OrganizationModel getOrganizationByName(RealmModel realm, String name);

  Stream<OrganizationModel> getOrganizationsStreamForDomain(
      RealmModel realm, String domain, boolean verified);

  Stream<OrganizationModel> getUserOrganizationsStream(RealmModel realm, UserModel user);

  Stream<OrganizationModel> searchForOrganizationStream(
      RealmModel realm,
      Map<String, String> attributes,
      Integer firstResult,
      Integer maxResults,
      Optional<UserModel> member,
      Boolean exact);

  Long getOrganizationsCount(
      RealmModel realm, String search, Map<String, String> attributes, Boolean exact);

  @Deprecated(forRemoval = true)
  default Long getOrganizationsCount(
      RealmModel realm, String search, Map<String, String> attributes) {
    return getOrganizationsCount(realm, search, attributes, false);
  }

  boolean removeOrganization(RealmModel realm, String id);

  void removeOrganizations(RealmModel realm);

  Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, UserModel user);

  Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, String email);

  InvitationModel getInvitationById(RealmModel realm, String id);

  Stream<IdentityProviderModel> getIdentityProvidersStream(
      RealmModel realm, String configKey, String configValue, boolean exact);

  Collection<? extends OrganizationModel> getOrganizationsMissingRole(
      String roleName, int batchSize);

  // https://github.com/p2-inc/keycloak-orgs/issues/454
  long countOrphanedOrganizations();

  // deprecated methods

  /**
   * @deprecated use {@link #searchForOrganizationStream searchForOrganizationStream} method instead
   */
  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> searchForOrganizationByNameStream(
      RealmModel realm, String search, Integer firstResult, Integer maxResults) {
    Map<String, String> attributes = Maps.newHashMap();
    if (!Strings.isNullOrEmpty(search)) {
      attributes.put("name", search);
    }
    return searchForOrganizationStream(
        realm, attributes, firstResult, maxResults, Optional.empty());
  }

  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> searchForOrganizationStream(
      RealmModel realm,
      Map<String, String> attributes,
      Integer firstResult,
      Integer maxResults,
      Optional<UserModel> member) {
    return searchForOrganizationStream(realm, attributes, firstResult, maxResults, member, false);
  }

  /**
   * @deprecated use {@link #searchForOrganizationStream searchForOrganizationStream} method instead
   */
  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> searchForOrganizationByAttributesStream(
      RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults) {
    return searchForOrganizationStream(
        realm, attributes, firstResult, maxResults, Optional.empty());
  }

  /**
   * @deprecated use {@link #searchForOrganizationStream searchForOrganizationStream} method instead
   */
  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> getOrganizationsStream(
      RealmModel realm, Integer firstResult, Integer maxResults) {
    return searchForOrganizationStream(realm, null, firstResult, maxResults, Optional.empty());
  }

  /**
   * @deprecated use {@link #searchForOrganizationStream searchForOrganizationStream} method instead
   */
  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> getOrganizationsStream(
      RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults) {
    return searchForOrganizationStream(
        realm, attributes, firstResult, maxResults, Optional.empty(), false);
  }

  /**
   * @deprecated use {@link #searchForOrganizationStream searchForOrganizationStream} method instead
   */
  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> getOrganizationsStream(RealmModel realm) {
    return searchForOrganizationStream(realm, null, null, null, Optional.empty());
  }

  Stream<ExtOrganizationEntity> findByNames(RealmModel realm, Set<String> names);

  default boolean any(RealmModel realm, Set<String> names) {
    return findByNames(realm, names).findAny().isPresent();
  }
}
