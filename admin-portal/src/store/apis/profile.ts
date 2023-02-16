import { emptySplitApi as api } from "./empty";
export const addTagTypes = ["Account"] as const;
const injectedRtkApi = api
  .enhanceEndpoints({
    addTagTypes,
  })
  .injectEndpoints({
    endpoints: (build) => ({
      getAccount: build.query<GetAccountApiResponse, GetAccountApiArg>({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/`,
          params: { userProfileMetadata: queryArg.userProfileMetadata },
        }),
        providesTags: ["Account"],
      }),
      updateAccount: build.mutation<
        UpdateAccountApiResponse,
        UpdateAccountApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/`,
          method: "POST",
          body: queryArg.accountRepresentation,
        }),
        invalidatesTags: ["Account"],
      }),
      getApplications: build.query<
        GetApplicationsApiResponse,
        GetApplicationsApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/applications`,
          params: { name: queryArg.name },
        }),
        providesTags: ["Account"],
      }),
      getConsent: build.query<GetConsentApiResponse, GetConsentApiArg>({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/applications/${queryArg.clientId}/consent`,
        }),
        providesTags: ["Account"],
      }),
      createConsent: build.mutation<
        CreateConsentApiResponse,
        CreateConsentApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/applications/${queryArg.clientId}/consent`,
          method: "POST",
        }),
        invalidatesTags: ["Account"],
      }),
      updateConsent: build.mutation<
        UpdateConsentApiResponse,
        UpdateConsentApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/applications/${queryArg.clientId}/consent`,
          method: "PUT",
        }),
        invalidatesTags: ["Account"],
      }),
      deleteConsent: build.mutation<
        DeleteConsentApiResponse,
        DeleteConsentApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/applications/${queryArg.clientId}/consent`,
          method: "DELETE",
        }),
        invalidatesTags: ["Account"],
      }),
      getCredentials: build.query<
        GetCredentialsApiResponse,
        GetCredentialsApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/credentials`,
          params: {
            type: queryArg["type"],
            "user-credentials": queryArg["user-credentials"],
          },
        }),
        providesTags: ["Account"],
      }),
      deleteCredential: build.mutation<
        DeleteCredentialApiResponse,
        DeleteCredentialApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/credentials/${queryArg.credentialId}`,
          method: "DELETE",
        }),
        invalidatesTags: ["Account"],
      }),
      updateCredentialLabel: build.mutation<
        UpdateCredentialLabelApiResponse,
        UpdateCredentialLabelApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/credentials/${queryArg.credentialId}/label`,
          method: "PUT",
          body: queryArg.body,
        }),
        invalidatesTags: ["Account"],
      }),
      getSessions: build.query<GetSessionsApiResponse, GetSessionsApiArg>({
        query: (queryArg) => ({ url: `/${queryArg.realm}/account/sessions` }),
        providesTags: ["Account"],
      }),
      deleteCurrentSession: build.mutation<
        DeleteCurrentSessionApiResponse,
        DeleteCurrentSessionApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/sessions`,
          method: "DELETE",
        }),
        invalidatesTags: ["Account"],
      }),
      getDevices: build.query<GetDevicesApiResponse, GetDevicesApiArg>({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/sessions/devices`,
        }),
        providesTags: ["Account"],
      }),
      deleteSession: build.mutation<
        DeleteSessionApiResponse,
        DeleteSessionApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/sessions/${queryArg.sessionId}`,
          method: "DELETE",
        }),
        invalidatesTags: ["Account"],
      }),
      getLinkedAccounts: build.query<
        GetLinkedAccountsApiResponse,
        GetLinkedAccountsApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/linked-accounts`,
        }),
        providesTags: ["Account"],
      }),
      deleteLinkedProvider: build.mutation<
        DeleteLinkedProviderApiResponse,
        DeleteLinkedProviderApiArg
      >({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/linked-accounts/${queryArg.providerId}`,
          method: "DELETE",
        }),
        invalidatesTags: ["Account"],
      }),
      getGroups: build.query<GetGroupsApiResponse, GetGroupsApiArg>({
        query: (queryArg) => ({
          url: `/${queryArg.realm}/account/groups`,
          params: { briefRepresentation: queryArg.briefRepresentation },
        }),
        providesTags: ["Account"],
      }),
    }),
    overrideExisting: false,
  });
export { injectedRtkApi as profileApi };
export type GetAccountApiResponse =
  /** status 200 success */ AccountRepresentation;
export type GetAccountApiArg = {
  /** realm name (not id!) */
  realm: string;
  userProfileMetadata?: boolean;
};
export type UpdateAccountApiResponse = unknown;
export type UpdateAccountApiArg = {
  /** realm name (not id!) */
  realm: string;
  accountRepresentation: AccountRepresentation;
};
export type GetApplicationsApiResponse =
  /** status 200 success */ ClientRepresentation[];
export type GetApplicationsApiArg = {
  /** realm name (not id!) */
  realm: string;
  name?: string;
};
export type GetConsentApiResponse =
  /** status 200 success */ ConsentRepresentation;
export type GetConsentApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** client id */
  clientId: string;
};
export type CreateConsentApiResponse =
  /** status 200 success */ ConsentRepresentation;
export type CreateConsentApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** client id */
  clientId: string;
};
export type UpdateConsentApiResponse =
  /** status 200 success */ ConsentRepresentation;
export type UpdateConsentApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** client id */
  clientId: string;
};
export type DeleteConsentApiResponse = unknown;
export type DeleteConsentApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** client id */
  clientId: string;
};
export type GetCredentialsApiResponse =
  /** status 200 success */ CredentialRepresentation[];
export type GetCredentialsApiArg = {
  /** realm name (not id!) */
  realm: string;
  type?: string;
  "user-credentials"?: boolean;
};
export type DeleteCredentialApiResponse = unknown;
export type DeleteCredentialApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** Credential ID */
  credentialId: string;
};
export type UpdateCredentialLabelApiResponse = unknown;
export type UpdateCredentialLabelApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** Credential ID */
  credentialId: string;
  body: string;
};
export type GetSessionsApiResponse =
  /** status 200 success */ SessionRepresentation[];
export type GetSessionsApiArg = {
  /** realm name (not id!) */
  realm: string;
};
export type DeleteCurrentSessionApiResponse = unknown;
export type DeleteCurrentSessionApiArg = {
  /** realm name (not id!) */
  realm: string;
};
export type GetDevicesApiResponse =
  /** status 200 success */ DeviceRepresentation[];
export type GetDevicesApiArg = {
  /** realm name (not id!) */
  realm: string;
};
export type DeleteSessionApiResponse = unknown;
export type DeleteSessionApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** Session ID */
  sessionId: string;
};
export type GetLinkedAccountsApiResponse =
  /** status 200 success */ LinkedAccountRepresentation[];
export type GetLinkedAccountsApiArg = {
  /** realm name (not id!) */
  realm: string;
};
export type DeleteLinkedProviderApiResponse = unknown;
export type DeleteLinkedProviderApiArg = {
  /** realm name (not id!) */
  realm: string;
  /** Provider ID */
  providerId: string;
};
export type GetGroupsApiResponse =
  /** status 200 success */ GroupRepresentation[];
export type GetGroupsApiArg = {
  /** realm name (not id!) */
  realm: string;
  briefRepresentation?: boolean;
};
export type UserProfileMetadataAttributeRepresentation = {
  name?: string;
  displayName?: string;
  required?: boolean;
  readOnly?: boolean;
  validators?: object;
};
export type UserProfileMetadataRepresentation = {
  attributes?: UserProfileMetadataAttributeRepresentation[];
};
export type AccountRepresentation = {
  id?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  emailVerified?: boolean;
  userProfileMetadata?: UserProfileMetadataRepresentation;
};
export type ConsentScopeRepresentation = {
  id?: string;
  name?: string;
  displayText?: string;
};
export type ConsentRepresentation = {
  createDate?: number;
  lastUpdatedDate?: number;
  grantedScopes?: ConsentScopeRepresentation[];
};
export type ClientRepresentation = {
  clientId?: string;
  clientName?: string;
  description?: string;
  userConsetRequired?: boolean;
  inUse?: boolean;
  offlineAccess?: boolean;
  rootUrl?: string;
  baseUrl?: string;
  effectiveUrl?: string;
  lgogUri?: string;
  policyUri?: string;
  tosUri?: string;
  consent?: ConsentRepresentation;
};
export type CredentialMetadataRepresentation = {
  id?: string;
  type?: string;
  userLabel?: string;
  createDate?: string;
  credentialData?: object;
};
export type UserCredentialMetadataRepresentation = {
  credential?: CredentialMetadataRepresentation;
};
export type CredentialRepresentation = {
  type?: string;
  category?: string;
  displayName?: string;
  helpText?: string;
  iconCssClass?: string;
  updateAction?: string;
  removeable?: boolean;
  userCredentialsMetadatas?: UserCredentialMetadataRepresentation[];
};
export type SessionRepresentation = {
  id?: string;
  ipAddress?: string;
  started?: number;
  lastAccess?: number;
  expires?: number;
  browser?: string;
  current?: boolean;
  clients?: ClientRepresentation[];
};
export type DeviceRepresentation = {
  id?: string;
  ipAddress?: string;
  os?: string;
  osVersion?: string;
  browser?: string;
  device?: string;
  lastAccess?: number;
  current?: boolean;
  mobile?: boolean;
  sessions?: SessionRepresentation[];
};
export type LinkedAccountRepresentation = {
  connected?: boolean;
  isSocial?: boolean;
  providerAlias?: string;
  providerName?: string;
  displayName?: string;
  linkedUsername?: string;
};
export type GroupRepresentation = {
  access?: {
    [key: string]: any;
  };
  attributes?: {
    [key: string]: any;
  };
  clientRoles?: {
    [key: string]: any;
  };
  id?: string;
  name?: string;
  path?: string;
  realmRoles?: string[];
  subGroups?: GroupRepresentation[];
};
export const {
  useGetAccountQuery,
  useUpdateAccountMutation,
  useGetApplicationsQuery,
  useGetConsentQuery,
  useCreateConsentMutation,
  useUpdateConsentMutation,
  useDeleteConsentMutation,
  useGetCredentialsQuery,
  useDeleteCredentialMutation,
  useUpdateCredentialLabelMutation,
  useGetSessionsQuery,
  useDeleteCurrentSessionMutation,
  useGetDevicesQuery,
  useDeleteSessionMutation,
  useGetLinkedAccountsQuery,
  useDeleteLinkedProviderMutation,
  useGetGroupsQuery,
} = injectedRtkApi;
