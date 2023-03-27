package io.phasetwo.service.model;

import com.google.common.collect.MoreCollectors;
import java.util.Set;
import java.util.stream.Stream;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderEvent;

public interface OrganizationModel extends WithAttributesModel {

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

  Stream<UserModel> getMembersStream();

  Stream<UserModel> searchForMembersStream(String search, Integer firstResult, Integer maxResults);

  boolean hasMembership(UserModel user);

  void grantMembership(UserModel user);

  void revokeMembership(UserModel user);

  Stream<InvitationModel> getInvitationsStream();

  default Stream<InvitationModel> getInvitationsByEmail(String email) {
    return getInvitationsStream().filter(i -> i.getEmail().equals(email));
  }

  void revokeInvitation(String id);

  void revokeInvitations(String email);

  InvitationModel addInvitation(String email, UserModel inviter);

  Stream<IdentityProviderModel> getIdentityProvidersStream();

  interface OrganizationEvent extends ProviderEvent {
    OrganizationModel getOrganization();

    KeycloakSession getKeycloakSession();

    RealmModel getRealm();
  }

  interface OrganizationCreationEvent extends OrganizationEvent {}

  interface OrganizationRemovedEvent extends OrganizationEvent {}
}
