// Need to use the React-specific entry point to import createApi
import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import config from "config";

interface FeatureFlagsState {
  name: string;
  displayName: string;
  logoUrl: string;
  faviconUrl: string;
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

// Define a service using a base URL and expected endpoints
export const featureFlagsApi = createApi({
  reducerPath: "featureFlagsApi",
  baseQuery: fetchBaseQuery({ baseUrl: `/${config.realm}` }),
  endpoints: (builder) => ({
    getFeatureFlags: builder.query<FeatureFlagsState, void>({
      query: () => `config.json`,
    }),
  }),
});

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const { useGetFeatureFlagsQuery } = featureFlagsApi;
