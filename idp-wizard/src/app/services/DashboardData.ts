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
  id?: string;
  time?: number;
  user?: string;
  eventType?: EventType;
  details?: {};
}

export const getEventData = async <IDashboardEvents>() => {
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] = useKeycloakAdminApi();
  
  await setKcAdminClientAccessToken();

  const events = await kcAdminClient.realms.findEvents({
    realm: getRealm()
  });

  const eventsToShow = events.filter(
    (event) =>
      event.details?.grant_type != "client_credentials" &&
      event.type !== "CODE_TO_TOKEN_ERROR" &&
      event.type !== "CODE_TO_TOKEN"
  );

  const allUsers = await kcAdminClient.users.find({
    realm: getRealm()
  });

  return eventsToShow.map(({ time, userId, type, details }) => {
    const user = allUsers.find((user) => user.id == userId);
    return {
      id: user?.id,
      time: time,
      user: user?.username,
      eventType: type,
      details: details,
    };
  });
};

export const getSummaryData = async <IDashboardSummaryData>() => {
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] = useKeycloakAdminApi();

  await setKcAdminClientAccessToken();
  const allUsers = await kcAdminClient.users.find();

  console.log("keycloak.realm", getRealm());
  const logins = await kcAdminClient.realms.findEvents({
    realm: getRealm(),
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
    realm: getRealm(),
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
