import { isEmpty } from "lodash";

export interface Environment {
  name: string;
  displayName: string;
  logoUrl: string;
  faviconUrl: string;
  appiconUrl: string;
  realm: string;
  locale: string;
  authServerUrl: string;
  baseUrl: string;
  resourceUrl: string;
  refererUrl: string;
  isRunningAsTheme: boolean;
  supportedLocales: {};
  features: Features;
}

export interface Features {
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

declare const environment: Environment;

const initialFeatures: Features = {
  profileEnabled: true,
  registrationEmailAsUsername: true,
  passwordUpdateAllowed: true,
  twoFactorUpdateAllowed: true,
  totpConfigured: true,
  passwordlessUpdateAllowed: true,
  deviceActivityEnabled: true,
  linkedAccountsEnabled: true,
  eventsEnabled: true,
  editUsernameAllowed: true,
  internationalizationEnabled: true,
  resourcesEnabled: true,
  viewGroupsEnabled: true,
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

const initialEnvironment: Environment = {
  name: "",
  displayName: "",
  logoUrl: "",
  faviconUrl: "",
  appiconUrl: "",
  realm: "test",
  locale: "en",
  authServerUrl: "https://app.phasetwo.io/auth/",
  baseUrl: "http://localhost:3000/",
  resourceUrl: ".",
  refererUrl: "",
  isRunningAsTheme: false,
  supportedLocales: { "en": "English" },
  features: initialFeatures,
};


var env: Environment = isEmpty(environment) ? initialEnvironment : environment;

export const windowBaseUrl: string =
  window.location.protocol +
  "//" +
  window.location.hostname +
  (window.location.port ? ":" + window.location.port : "") +
  (window.location.pathname.includes("/auth/") ? "/auth/realms" : "/realms");

export const windowRealm: string = (function (): string {
  let segments = window.location.pathname.split("/");
  for (let i = 0; i < segments.length; i++) {
    if (segments[i] === "realms" && segments.length > i + 1) {
      return segments[i + 1];
    }
  }
  return "";
})();

export const config = {
  basename: new URL(env.baseUrl).pathname,
  realm: "test" ?? windowRealm,
  env: env,
};
