package io.phasetwo.service.model;

import com.google.common.collect.MoreCollectors;
import java.util.Set;
import java.util.stream.Stream;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderEvent;

public interface OrganizationModel extends WithAttributes {

  String getId();

  String getName();

  void setName(String name);

  String getDisplayName();

  void setDisplayName(String displayName);

  Set<String> getDomains();

  void setDomains(Set<String> domain);

  DomainModel getDomain(String name);

  String getUrl();

  void setUrl(String url);

  RealmModel getRealm();

  UserModel getCreatedBy();

  default Long getMembersCount() {
    return getMembersCount(false);
  }

  default Stream<UserModel> getMembersStream() {
    return getMembersStream(false);
  }

  default Stream<UserModel> searchForMembersStream(
      String search, Integer firstResult, Integer maxResults) {
    return searchForMembersStream(search, firstResult, maxResults, false);
  }

  Long getMembersCount(boolean excludeAdminAccounts);

  Stream<UserModel> getMembersStream(boolean excludeAdminAccounts);

  Stream<UserModel> searchForMembersStream(
      String search, Integer firstResult, Integer maxResults, boolean excludeAdminAccounts);

  Stream<OrganizationMembershipModel> getOrganizationMembersStream();

  Stream<OrganizationMembershipModel> searchForOrganizationMembersStream(String search, Integer firstResult, Integer maxResults);

  boolean hasMembership(UserModel user);

  void grantMembership(UserModel user);

  void revokeMembership(UserModel user);

  Long getInvitationsCount();

  Stream<InvitationModel> getInvitationsStream();

  default Stream<InvitationModel> getInvitationsByEmail(String email) {
    return getInvitationsStream().filter(i -> i.getEmail().equals(email));
  }

  InvitationModel getInvitation(String id);

  void revokeInvitation(String id);

  void revokeInvitations(String email);

  InvitationModel addInvitation(String email, UserModel inviter);

  Stream<OrganizationRoleModel> getRolesStream();

  Stream<OrganizationRoleModel> getRolesByUserStream(UserModel user);

  default OrganizationRoleModel getRoleByName(String name) {
    return getRolesStream()
        .filter(r -> name.equals(r.getName()))
        .collect(MoreCollectors.toOptional())
        .orElse(null);
  }

  OrganizationMembershipModel getMembershipDetails(UserModel user);

  void removeRole(String name);

  OrganizationRoleModel addRole(String name);

  Stream<IdentityProviderModel> getIdentityProvidersStream();

  interface OrganizationEvent extends ProviderEvent {
    OrganizationModel getOrganization();

    KeycloakSession getKeycloakSession();

    RealmModel getRealm();
  }

  interface OrganizationCreationEvent extends OrganizationEvent {}

  interface OrganizationRemovedEvent extends OrganizationEvent {}
}
