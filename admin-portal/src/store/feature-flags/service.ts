// Need to use the React-specific entry point to import createApi
import { emptySplitApi as api } from "../apis/empty";

import config from "config";
export interface FeatureFlagsState {
  name: string;
  displayName: string;
  logoUrl: string;
  faviconUrl: string;
  appiconUrl: string;
  profileEnabled: boolean;
  registrationEmailAsUsername: boolean;
  passwordUpdateAllowed: boolean;
  twoFactorUpdateAllowed: boolean;
  totpConfigured: boolean;
  passwordlessUpdateAllowed: boolean;
  deviceActivityEnabled: boolean;
  linkedAccountsEnabled: boolean;
  eventsEnabled: boolean;
  editUsernameAllowed: boolean;
  internationalizationEnabled: boolean;
  resourcesEnabled: boolean;
  viewGroupsEnabled: boolean;
  deleteAccountAllowed: boolean;
  updateEmailFeatureEnabled: boolean;
  updateEmailActionEnabled: boolean;
  organizationsEnabled: boolean;
  orgDetailsEnabled: boolean;
  orgMembersEnabled: boolean;
  orgInvitationsEnabled: boolean;
  orgDomainsEnabled: boolean;
  orgSsoEnabled: boolean;
  orgEventsEnabled: boolean;
}

export const featureFlagsApi = api
  .enhanceEndpoints({ addTagTypes: ["FeatureFlags"] })
  .injectEndpoints({
    endpoints: (build) => ({
      getFeatureFlags: build.query<FeatureFlagsState, void>({
        query: () => `${config.realm}/config.json`,
        providesTags: ["FeatureFlags"],
      }),
    }),
  });

export const { useGetFeatureFlagsQuery } = featureFlagsApi;
