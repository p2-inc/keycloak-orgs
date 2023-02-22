import { createSlice } from "@reduxjs/toolkit";
import { RootState } from "store";
import { featureFlagsApi } from "./service";
import { FeatureFlagsState } from "./service";

const initialState = {
  name: "",
  displayName: "",
  logoUrl: "",
  faviconUrl: "",
  profileEnabled: true,
  registrationEmailAsUsername: false,
  passwordUpdateAllowed: true,
  twoFactorUpdateAllowed: true,
  totpConfigured: false,
  passwordlessUpdateAllowed: true,
  deviceActivityEnabled: true,
  linkedAccountsEnabled: true,
  eventsEnabled: true,
  editUsernameAllowed: false,
  internationalizationEnabled: false,
  resourcesEnabled: false,
  viewGroupsEnabled: false,
  deleteAccountAllowed: true,
  updateEmailFeatureEnabled: true,
  updateEmailActionEnabled: true,
  organizationsEnabled: true,
  orgDetailsEnabled: true,
  orgMembersEnabled: true,
  orgInvitationsEnabled: true,
  orgDomainsEnabled: true,
  orgSsoEnabled: true,
  orgEventsEnabled: true,
};

const slice = createSlice({
  name: "featureFlags",
  initialState: initialState as FeatureFlagsState,
  reducers: {},
  extraReducers: (builder) => {
    builder.addMatcher(
      featureFlagsApi.endpoints.getFeatureFlags.matchFulfilled,
      (state, { payload }) => {
        state.name = payload.name;
        state.displayName = payload.displayName;
        state.logoUrl = payload.logoUrl;
        state.faviconUrl = payload.faviconUrl;
        state.profileEnabled = payload.profileEnabled;
        state.registrationEmailAsUsername = payload.registrationEmailAsUsername;
        state.passwordUpdateAllowed = payload.passwordUpdateAllowed;
        state.twoFactorUpdateAllowed = payload.twoFactorUpdateAllowed;
        state.totpConfigured = payload.totpConfigured;
        state.passwordlessUpdateAllowed = payload.passwordlessUpdateAllowed;
        state.deviceActivityEnabled = payload.deviceActivityEnabled;
        state.linkedAccountsEnabled = payload.linkedAccountsEnabled;
        state.eventsEnabled = payload.eventsEnabled;
        state.editUsernameAllowed = payload.editUsernameAllowed;
        state.internationalizationEnabled = payload.internationalizationEnabled;
        state.resourcesEnabled = payload.resourcesEnabled;
        state.viewGroupsEnabled = payload.viewGroupsEnabled;
        state.deleteAccountAllowed = payload.deleteAccountAllowed;
        state.updateEmailFeatureEnabled = payload.updateEmailFeatureEnabled;
        state.updateEmailActionEnabled = payload.updateEmailActionEnabled;
        state.organizationsEnabled = payload.organizationsEnabled;
        state.orgDetailsEnabled = payload.orgDetailsEnabled;
        state.orgMembersEnabled = payload.orgMembersEnabled;
        state.orgInvitationsEnabled = payload.orgInvitationsEnabled;
        state.orgDomainsEnabled = payload.orgDomainsEnabled;
        state.orgSsoEnabled = payload.orgSsoEnabled;
        state.orgEventsEnabled = payload.orgEventsEnabled;
      }
    );
  },
});

export default slice.reducer;

export const selectFeatureFlags = (state: RootState) => state.featureFlags;
