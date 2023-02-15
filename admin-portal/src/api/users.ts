import { apiConfig, apiRealm } from "./helpers";
import {
  AccountApi,
  AccountRepresentation,
  CredentialRepresentation,
  ClientRepresentation,
  DeviceRepresentation,
  GroupRepresentation,
  SessionRepresentation,
  LinkedAccountRepresentation,
} from "@p2-inc/js-sdk";

const accountApi = new AccountApi(apiConfig);

export const Users = {
  getAccount: async function (): Promise<AccountRepresentation> {
    return accountApi.getAccount({
      realm: apiRealm,
      userProfileMetadata: true,
    });
  },

  updateAccount: async function (
    account: AccountRepresentation
  ): Promise<void> {
    return accountApi.updateAccount({
      realm: apiRealm,
      accountRepresentation: account,
    });
  },

  getCredentials: async function (): Promise<Array<CredentialRepresentation>> {
    return accountApi.getCredentials({ realm: apiRealm });
  },

  deleteCredential: async function (credentialId: string): Promise<void> {
    return accountApi.deleteCredential({
      realm: apiRealm,
      credentialId: credentialId,
    });
  },

  getApplications: async function (): Promise<Array<ClientRepresentation>> {
    return accountApi.getApplications({ realm: apiRealm });
  },

  getDevices: async function (): Promise<Array<DeviceRepresentation>> {
    return accountApi.getDevices({ realm: apiRealm });
  },

  getSessions: async function (): Promise<Array<SessionRepresentation>> {
    return accountApi.getSessions({ realm: apiRealm });
  },

  deleteCurrentSession: async function (): Promise<void> {
    return accountApi.deleteCurrentSession({ realm: apiRealm });
  },

  deleteSession: async function (sessionId: string): Promise<void> {
    return accountApi.deleteSession({ realm: apiRealm, sessionId: sessionId });
  },

  getLinkedAccounts: async function (): Promise<
    Array<LinkedAccountRepresentation>
  > {
    return accountApi.getLinkedAccounts({ realm: apiRealm });
  },

  deleteLinkedAccount: async function (providerId: string): Promise<void> {
    return accountApi.deleteLinkedProvider({
      realm: apiRealm,
      providerId: providerId,
    });
  },
};
