import { PATHS } from "@app/routes";
import { useGetFeatureFlagsQuery } from "@app/services";
import { useKeycloak } from "@react-keycloak/web";
import { useEffect, useState } from "react";
import { generatePath, useNavigate, useParams } from "react-router-dom";

const requiredResourceRoles = [
  "manage-identity-providers",
  "view-identity-providers",
  "query-users",
  "view-users",
  "view-events",
  "view-realm",
  "manage-realm",
];

const requiredOrganizationRoles = [
  "view-organization",
  "manage-organization",
  "view-identity-providers",
  "manage-identity-providers",
];

export function useRoleAccess() {
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const { keycloak } = useKeycloak();
  let { realm } = useParams();
  let navigate = useNavigate();
  // Starts as null to make true/false explicit states
  const [hasAccess, setHasAccess] = useState<null | boolean>(null);

  function navigateToAccessDenied() {
    window.location.replace(
      generatePath(PATHS.accessDenied, {
        realm,
      })
    );
  }

  function hasOrganizationRole(role) {
    const orgId = keycloak?.tokenParsed?.org_id;
    const orgs = keycloak?.tokenParsed?.organizations;
    if (orgId == null || orgs == null) return false;
    const roles = orgs[orgId].roles;
    if (roles.indexOf(role) > -1) return true;
    else return false;
  }

  useEffect(() => {
    if (!featureFlags) return;
    //console.log("access control", featureFlags?.apiMode);
    //cloud mode
    if (featureFlags?.apiMode === "cloud") {
      let orgAccess: boolean[] = [];
      requiredOrganizationRoles.map((role) => {
        return orgAccess.push(hasOrganizationRole(role));
      });
      setHasAccess(!orgAccess.includes(false));
    } else {
      // onprem mode
      // if the keycloak realm is the master realm, then look at the <path-realm>-realm resource-roles rather than "realm-management"
      const resource =
        keycloak.realm === "master" ? `${realm}-realm` : "realm-management";
      let roleAccess: boolean[] = [];
      requiredResourceRoles.map((role) => {
        return roleAccess.push(keycloak.hasResourceRole(role, resource));
      });
      setHasAccess(!roleAccess.includes(false));
    }
  }, [keycloak?.token]);

  useEffect(() => {
    if (hasAccess === false && realm) {
      navigateToAccessDenied();
    }
  }, [hasAccess, realm]);

  return { hasAccess, navigateToAccessDenied };
}
