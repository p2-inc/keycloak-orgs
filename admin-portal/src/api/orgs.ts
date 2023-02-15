import { apiConfig, apiRealm } from "./helpers";
import {
  IdentityProvidersApi,
  OrganizationsApi,
  OrganizationDomainsApi,
  OrganizationInvitationsApi,
  OrganizationMembershipsApi,
  OrganizationRolesApi,
  UsersApi,
  IdentityProviderMapperRepresentation,
  IdentityProviderRepresentation,
  InvitationRepresentation,
  InvitationRequestRepresentation,
  MagicLinkRepresentation,
  OrganizationDomainRepresentation,
  OrganizationRepresentation,
  OrganizationRoleRepresentation,
  UserRepresentation,
} from "@p2-inc/js-sdk";

const identityProvidersApi = new IdentityProvidersApi(apiConfig);
const organizationsApi = new OrganizationsApi(apiConfig);
const domainsApi = new OrganizationDomainsApi(apiConfig);
const invitationsApi = new OrganizationInvitationsApi(apiConfig);
const membershipsApi = new OrganizationMembershipsApi(apiConfig);
const rolesApi = new OrganizationRolesApi(apiConfig);
const usersApi = new UsersApi(apiConfig);

export type InviteDetails = {
  email: string;
  inviterId: string;
  roles: {
    [roleName: string]: string;
  };
};

export const Orgs = {
  getMembers: async function (
    orgId: string
  ): Promise<Array<UserRepresentation>> {
    return membershipsApi.getOrganizationMemberships({
      realm: apiRealm,
      orgId: orgId,
    });
  },

  addMember: async function (orgId: string, userId: string): Promise<void> {
    return membershipsApi.addOrganizationMember({
      realm: apiRealm,
      orgId: orgId,
      userId: userId,
    });
  },

  removeMember: async function (orgId: string, userId: string): Promise<void> {
    return membershipsApi.removeOrganizationMember({
      realm: apiRealm,
      orgId: orgId,
      userId: userId,
    });
  },

  checkMembership: async function (
    orgId: string,
    userId: string
  ): Promise<Boolean> {
    return membershipsApi
      .checkOrganizationMembershipRaw({
        realm: apiRealm,
        orgId: orgId,
        userId: userId,
      })
      .then((resp) => {
        return resp.raw.status == 204;
      });
  },

  getRoles: async function (
    orgId: string
  ): Promise<Array<OrganizationRoleRepresentation>> {
    return rolesApi.getOrganizationRoles({ realm: apiRealm, orgId: orgId });
  },

  checkUserRole: async function (
    orgId: string,
    userId: string,
    name: string
  ): Promise<Boolean> {
    return rolesApi
      .checkUserOrganizationRoleRaw({
        realm: apiRealm,
        orgId: orgId,
        userId: userId,
        name: name,
      })
      .then((resp) => {
        return resp.raw.status == 204;
      });
  },

  grantUserRole: async function (
    orgId: string,
    userId: string,
    name: string
  ): Promise<void> {
    return rolesApi.grantUserOrganizationRole({
      realm: apiRealm,
      orgId: orgId,
      userId: userId,
      name: name,
    });
  },

  revokeUserRole: async function (
    orgId: string,
    userId: string,
    name: string
  ): Promise<void> {
    return rolesApi.revokeUserOrganizationRole({
      realm: apiRealm,
      orgId: orgId,
      userId: userId,
      name: name,
    });
  },

  getUserRoles: async function (
    orgId: string,
    userId: string
  ): Promise<Array<OrganizationRoleRepresentation>> {
    return usersApi.realmUsersUserIdOrgsOrgIdRolesGet({
      realm: apiRealm,
      orgId: orgId,
      userId: userId,
    });
  },

  getOrgs: async function (): Promise<Array<OrganizationRepresentation>> {
    return organizationsApi.getOrganizations({ realm: apiRealm });
  },

  getOrg: async function (orgId: string): Promise<OrganizationRepresentation> {
    return organizationsApi.getOrganizationById({
      realm: apiRealm,
      orgId: orgId,
    });
  },

  createOrg: async function (name: string, displayName: string): Promise<void> {
    return organizationsApi.createOrganization({
      realm: apiRealm,
      organizationRepresentation: { name: name, displayName: displayName },
    });
  },

  editOrg: async function (org: OrganizationRepresentation): Promise<void> {
    return organizationsApi.updateOrganization({
      realm: apiRealm,
      orgId: org.id ?? "",
      organizationRepresentation: org,
    });
  },

  getIdentityProviders: async function (
    orgId: string
  ): Promise<Array<IdentityProviderRepresentation>> {
    return identityProvidersApi.getIdps({ realm: apiRealm, orgId: orgId });
  },

  getInvitations: async function (
    orgId: string
  ): Promise<Array<InvitationRepresentation>> {
    return invitationsApi.getOrganizationInvitations({
      realm: apiRealm,
      orgId: orgId,
    });
  },

  inviteUser: async function (
    orgId: string,
    invitation: InvitationRequestRepresentation
  ): Promise<void> {
    return invitationsApi.addOrganizationInvitation({
      realm: apiRealm,
      orgId: orgId,
      invitationRequestRepresentation: invitation,
    });
  },

  removeInvitation: async function (
    orgId: string,
    invitationId: string
  ): Promise<void> {
    return invitationsApi.removeOrganizationInvitation({
      realm: apiRealm,
      orgId: orgId,
      invitationId: invitationId,
    });
  },

  getDomains: async function (
    orgId: string
  ): Promise<Array<OrganizationDomainRepresentation>> {
    return domainsApi.getOrganizationDomains({ realm: apiRealm, orgId: orgId });
  },

  getDomain: async function (
    orgId: string,
    domain: string
  ): Promise<OrganizationDomainRepresentation> {
    return domainsApi.getOrganizationDomain({
      realm: apiRealm,
      orgId: orgId,
      domainName: domain,
    });
  },

  verifyDomain: async function (orgId: string, domain: string): Promise<void> {
    return domainsApi.verifyDomain({
      realm: apiRealm,
      orgId: orgId,
      domainName: domain,
    });
  },

  removeDomain: async function (orgId: string, domain: string): Promise<void> {
    this.getOrg(orgId).then((org) => {
      org.domains = org.domains?.filter((d) => d != domain);
      return this.editOrg(org);
    });
  },
};
