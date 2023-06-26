package io.phasetwo.service.model;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

public interface OrganizationProvider extends Provider {

  OrganizationModel createOrganization(
      RealmModel realm, String name, UserModel createdBy, boolean admin);

  OrganizationModel getOrganizationById(RealmModel realm, String id);

  Stream<OrganizationModel> getOrganizationsStreamForDomain(
      RealmModel realm, String domain, boolean verified);

  Stream<OrganizationModel> getUserOrganizationsStream(RealmModel realm, UserModel user);

  Stream<OrganizationModel> searchForOrganizationStream(
      RealmModel realm,
      Map<String, String> attributes,
      Integer firstResult,
      Integer maxResults,
      Optional<UserModel> member);

  Long getOrganizationsCount(RealmModel realm, String search);

  boolean removeOrganization(RealmModel realm, String id);

  void removeOrganizations(RealmModel realm);

  Stream<InvitationModel> getUserInvitationsStream(RealmModel realm, UserModel user);

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
        realm, attributes, firstResult, maxResults, Optional.empty());
  }

  /**
   * @deprecated use {@link #searchForOrganizationStream searchForOrganizationStream} method instead
   */
  @Deprecated(forRemoval = true)
  default Stream<OrganizationModel> getOrganizationsStream(RealmModel realm) {
    return searchForOrganizationStream(realm, null, null, null, Optional.empty());
  }
}
