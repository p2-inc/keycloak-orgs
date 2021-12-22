import EventType from "@keycloak/keycloak-admin-client/lib/defs/eventTypes";
import { useKeycloakAdminApi } from "../hooks/useKeycloakAdminApi";

export interface IDashboardSummaryData {
  loginsToday: number;
  loginsThisWeek: number;
  users: number;
  groups: number;
  failedLogins: number;
  usersLockedOut: number;
}

export interface IDashboardEvents {
  time?: number;
  user?: string;
  eventType?: EventType;
  details?: {};
}

export const getEventData = async <IDashboardEvents>() => {
  const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();

  //Get events
  const events = await kcAdminClient.realms.findEvents({
    realm: process.env.REALM || "wizard",
  });
  const eventsToShow = events.filter(
    (event) =>
      event.details?.grant_type != "client_credentials" &&
      event.type !== "CODE_TO_TOKEN_ERROR" &&
      event.type !== "CODE_TO_TOKEN"
  );

  const allUsers = await kcAdminClient.users.find();

  return eventsToShow.map((event) => {
    return {
      time: event.time,
      user: allUsers.find((user) => user.id == event.userId)?.username,
      eventType: event.type,
      details: event.details,
    };
  });
};

export const getSummaryData = async <IDashboardSummaryData>() => {
  const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();

  await setKcAdminClientAccessToken();
  const allUsers = await kcAdminClient.users.find();

  const logins = await kcAdminClient.realms.findEvents({
    realm: process.env.REALM!,
    type: "LOGIN",
  });
  const loginsThisWeek = logins.filter(
    (event) =>
      event.details?.grant_type != "client_credentials" &&
      new Date(Date.now() - 7 * 24 * 60 * 60 * 1000) <=
        new Date(event.time || "")
  );
  const loginsToday = logins.filter(
    (event) =>
      event.details?.grant_type != "client_credentials" &&
      new Date(event.time || "").toLocaleDateString() ==
        new Date().toLocaleDateString()
  );
  const failedLogins = await kcAdminClient.realms.findEvents({
    realm: process.env.REALM!,
    type: "LOGIN_ERROR",
  });
  const lockedOutUsers = allUsers.filter((user) => !user.enabled);
  const groups = await kcAdminClient.groups.find();

  return {
    loginsToday: loginsToday.length,
    loginsThisWeek: loginsThisWeek.length,
    users: allUsers.length,
    groups: groups.length,
    failedLogins: failedLogins.length,
    usersLockedOut: lockedOutUsers.length,
  };
};
export const getKeycloakUsers = async () => {
  const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();
  const allUsers = await kcAdminClient.users.find();
  return allUsers;
};
