import { getRealmFromPath, PATHS } from "@app/routes";
import { useKeycloak } from "@react-keycloak/web";
import { useEffect, useState } from "react";
import { generatePath, useParams } from "react-router-dom";
import { useAppSelector } from "./hooks";

// Logic for role checks
// 1. Check if the user has the required roles for the realm admin. This sets
//    access for ability to set Realm level IDP
// 2. Check if the user has the required roles for the organization admin. This sets
//    access for ability to set Organization level IDP
// 3. If the user has access to more than 1 org or to 1 org and realm level, show the selector
//    for the org picker to allow the choice.
// Choice is saved locally. On keycloak token refreshes, access is rechecked.

export const requiredOrganizationResourceRoles = [
  "view-identity-providers",
  "manage-identity-providers",
  "query-users",
  "view-users",
  "view-events",
  "view-realm",
  "manage-realm",
];

export const requiredOrganizationAdminRoles = [
  "view-organization",
  "manage-organization",
  "view-identity-providers",
  "manage-identity-providers",
];

export const requiredRealmResourceRoles = [
  "view-identity-providers",
  "manage-identity-providers",
  "query-users",
  "view-users",
  "view-events",
  "view-realm",
  "manage-realm",
];

export function useRoleAccess() {
  const apiMode = useAppSelector((state) => state.settings.apiMode);
  const currentOrg = useAppSelector((state) => state.settings.currentOrg);
  const { keycloak } = useKeycloak();
  let { realm: pathRealm } = useParams();

  // Due to load order, the dynamic path is not always available.
  // Which means the realm is not always available.
  // This is a workaround to get the realm from the path
  // if it is not available in the params.
  const realm = pathRealm || getRealmFromPath();

  // Starts as null to make true/false explicit states
  const [hasOrgAccess, setHasOrgAccess] = useState<null | boolean>(null);

  function navigateToAccessDenied() {
    if (window.location.pathname.endsWith("access-denied")) return;

    window.location.assign(
      generatePath(PATHS.accessDenied, {
        realm: realm || window.location.pathname.split("/")[3],
      })
    );
  }

  function hasRealmRole(role) {
    const realmAccessRoles = keycloak?.tokenParsed?.resource_access;

    if (realmAccessRoles === null || realmAccessRoles === undefined)
      return false;
    if (realmAccessRoles[`${realm}-realm`]?.roles.includes(role)) return true;
    if (realmAccessRoles["realm-management"]?.roles.includes(role)) return true;
    else return false;
  }

  function hasRealmRoles() {
    let roleAccess: boolean[] = [];
    let roleGroupUsage = requiredRealmResourceRoles;

    roleGroupUsage.map((role) => {
      return roleAccess.push(hasRealmRole(role));
    });

    return !roleAccess.includes(false);
  }

  function hasOrganizationRole(role, orgId) {
    const orgs = keycloak?.tokenParsed?.organizations;
    if (
      orgId === null ||
      orgId === undefined ||
      orgs === null ||
      orgs === undefined
    )
      return false;
    const roles = orgs[orgId]?.roles;
    if (roles && roles.indexOf(role) > -1) return true;
    else return false;
  }

  function hasOrganizationRoles(roleGroup: "admin" | "resource", orgId) {
    let roleAccess: boolean[] = [];
    let roleGroupUsage;
    if (roleGroup === "admin") {
      roleGroupUsage = requiredOrganizationAdminRoles;
    }
    if (roleGroup === "resource") {
      roleGroupUsage = requiredOrganizationResourceRoles;
    }
    roleGroupUsage.map((role) => {
      return roleAccess.push(hasOrganizationRole(role, orgId));
    });
    return !roleAccess.includes(false);
  }

  // TODO: is this fully sufficient to refresh the choice of org?
  // may need to force refresh
  function checkAccess() {
    if (currentOrg === "global") {
      setHasOrgAccess(true);
      return;
    }

    //cloud mode
    if (apiMode === "cloud") {
      setHasOrgAccess(hasOrganizationRoles("admin", currentOrg));
    } else {
      // onprem mode
      // if the keycloak realm is the master realm,
      // then look at the < path - realm > -realm resource - roles rather than "realm-management"
      const resource =
        keycloak.realm === "master" ? `${realm}-realm` : "realm-management";
      let roleAccess: boolean[] = [];
      requiredOrganizationResourceRoles.map((role) => {
        return roleAccess.push(keycloak.hasResourceRole(role, resource));
      });
      // TODO: What is the point of doing two checks for org access that set the same thing?
      setHasOrgAccess(!roleAccess.includes(false));
      setHasOrgAccess(hasOrganizationRoles("resource", currentOrg));
    }
  }

  useEffect(() => {
    checkAccess();
  }, [keycloak?.tokenParsed, apiMode]);

  useEffect(() => {
    checkAccess();
  }, []);

  return {
    hasOrgAccess,
    hasOrganizationRoles,
    hasRealmRoles,
    navigateToAccessDenied,
  };
}
