import { PATHS } from "@app/routes";
import { useKeycloak } from "@react-keycloak/web";
import { useEffect, useState } from "react";
import { generatePath, useParams } from "react-router-dom";
import { useAppSelector } from "./hooks";

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
  let { realm } = useParams();
  // Starts as null to make true/false explicit states
  const [hasOrgAccess, setHasOrgAccess] = useState<null | boolean>(null);

  function navigateToAccessDenied() {
    window.location.replace(
      generatePath(PATHS.accessDenied, {
        realm,
      })
    );
  }

  function hasRealmRole(role) {
    const realmAccessRoles = keycloak?.tokenParsed?.resource_access;
    // console.log("hasRealmRole", role, realmAccessRoles);

    if (realmAccessRoles === null || realmAccessRoles === undefined)
      return false;
    if (realmAccessRoles["realm-management"]?.roles.indexOf(role) > -1)
      return true;
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
    // console.log("hasOrganizationRole", role, orgId);
    const orgs = keycloak?.tokenParsed?.organizations;
    if (orgId === null || orgId === undefined || orgs === null) return false;
    const roles = orgs[orgId]?.roles;
    if (roles && roles.indexOf(role) > -1) return true;
    else return false;
  }

  function hasOrganizationRoles(roleGroup: "admin" | "resource", orgId) {
    // console.log("[hasOrganizationRoles]", roleGroup, orgId);
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

  // Can't use this here in order to get correct role access for use with
  // the Org Selector
  // useEffect(() => {
  //   if (hasOrgAccess === false && realm) {
  //     navigateToAccessDenied();
  //   }
  // }, [hasOrgAccess, realm]);

  return {
    hasOrgAccess,
    hasOrganizationRoles,
    hasRealmRoles,
    navigateToAccessDenied,
  };
}
