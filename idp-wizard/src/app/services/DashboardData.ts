import EventRepresentation from "@keycloak/keycloak-admin-client/lib/defs/eventRepresentation";
import EventType from "@keycloak/keycloak-admin-client/lib/defs/eventTypes";
import GroupRepresentation from "@keycloak/keycloak-admin-client/lib/defs/groupRepresentation";
import UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import { useEffect, useState } from "react";
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

export interface DashboardActivity {
  activityData: IDashboardEvents[];
  loading: boolean;
}

export const useEventData = <DashboardActivity>() => {
  const { kcAdminClient, setKcAdminClientAccessToken, getRealm } =
    useKeycloakAdminApi();
  const realm = getRealm()!;

  const [events, setEvents] = useState<EventRepresentation[]>([]);
  const [users, setUsers] = useState<UserRepresentation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setKcAdminClientAccessToken();

    const fetchEvents = async () => {
      const events = await kcAdminClient.realms.findEvents({
        realm,
      });
      setEvents(events);
    };
    fetchEvents();

    const fetchUsers = async () => {
      const allUsers = await kcAdminClient.users.find({
        realm,
      });
      setUsers(allUsers);
    };
    fetchUsers();

    setLoading(false);
  }, []);

  const eventsToShow = events
    .filter(
      (event) =>
        event.details?.grant_type != "client_credentials" &&
        event.type !== "CODE_TO_TOKEN_ERROR" &&
        event.type !== "CODE_TO_TOKEN"
    )
    .map(({ time, userId, type, details }) => {
      const user = users.find((user) => user.id == userId);
      return {
        id: user?.id,
        time: time,
        user: user?.username,
        eventType: type,
        details: details,
      };
    });

  return {
    activityData: eventsToShow,
    loading,
  };
};

export const useSummaryData = <IDashboardSummaryData>() => {
  const { kcAdminClient, setKcAdminClientAccessToken, getRealm } =
    useKeycloakAdminApi();
  const realm = getRealm()!;
  const [loading, setLoading] = useState(true);

  const [allUsers, setAllUsers] = useState<UserRepresentation[]>([]);
  const [logins, setLogins] = useState<EventRepresentation[]>([]);
  const [failedLogins, setFailedLogins] = useState<EventRepresentation[]>([]);
  const [groups, setGroups] = useState<GroupRepresentation[]>([]);

  useEffect(() => {
    setKcAdminClientAccessToken();

    const fetchUsers = async () => {
      const resp = await kcAdminClient.users.find();
      setAllUsers(resp);
    };
    fetchUsers();

    const findLoginEvents = async () => {
      const logins = await kcAdminClient.realms.findEvents({
        realm,
        type: "LOGIN",
      });
      setLogins(logins);
    };
    findLoginEvents();

    const findLoginErrorEvents = async () => {
      const failedLogins = await kcAdminClient.realms.findEvents({
        realm,
        type: "LOGIN_ERROR",
      });
      setFailedLogins(failedLogins);
    };
    findLoginErrorEvents();

    const findGroups = async () => {
      const kcGroups = await kcAdminClient.groups.find();
      setGroups(kcGroups);
    };
    findGroups();

    setLoading(false);
  }, []);

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
  const lockedOutUsers = allUsers.filter((user) => !user.enabled);

  return {
    loginsToday: loginsToday.length,
    loginsThisWeek: loginsThisWeek.length,
    users: allUsers.length,
    groups: groups.length,
    failedLogins: failedLogins.length,
    usersLockedOut: lockedOutUsers.length,
    loading,
  };
};

export const getKeycloakUsers = async () => {
  const { kcAdminClient, setKcAdminClientAccessToken } = useKeycloakAdminApi();
  await setKcAdminClientAccessToken();
  const allUsers = await kcAdminClient.users.find();
  return allUsers;
};
