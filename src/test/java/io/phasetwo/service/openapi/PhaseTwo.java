package io.phasetwo.service.openapi;

import io.phasetwo.service.KeycloakSuite;
import io.phasetwo.service.openapi.api.*;
import org.keycloak.admin.client.Keycloak;

import java.net.URI;

public class PhaseTwo {

  private final Keycloak keycloak;
  private final URI absoluteUri;

  public PhaseTwo(KeycloakSuite suite) {
    this(suite.client(), suite.getAuthUrl());
  }

  public PhaseTwo(Keycloak keycloak, String absoluteUrl) {
    this.keycloak = keycloak;
    this.absoluteUri = URI.create(absoluteUrl).resolve("/auth/realms");
  }

  private  <T> T buildApi(Class<T> targetClass) {
    return keycloak.proxy(targetClass, absoluteUri);
  }

  public OrganizationInvitationsApi invitations() {
    return buildApi(OrganizationInvitationsApi.class);
  }

  public UsersApi users() {
    return buildApi(UsersApi.class);
  }

  public OrganizationRolesApi roles() {
    return buildApi(OrganizationRolesApi.class);
  }

  public OrganizationMembershipsApi members() {
    return buildApi(OrganizationMembershipsApi.class);
  }

  public OrganizationsApi organizations() {
    return buildApi(OrganizationsApi.class);
  }

  public IdentityProvidersApi idps() {
    return buildApi(IdentityProvidersApi.class);
  }

  public OrganizationDomainsApi domains() {
    return buildApi(OrganizationDomainsApi.class);
  }
}
