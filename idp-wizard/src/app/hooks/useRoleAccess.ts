import { PATHS } from "@app/routes";
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

export function useRoleAccess() {
  const { keycloak } = useKeycloak();
  let { realm } = useParams();
  let navigate = useNavigate();
  // Starts as null to make true/false explicit states
  const [hasAccess, setHasAccess] = useState<null | boolean>(null);

  function navigateToAccessDenied() {
    navigate(
      generatePath(PATHS.accessDenied, {
        realm,
      })
    );
  }

  useEffect(() => {
    // token was authenticated or refreshed
    // if the keycloak realm is the master realm, then look at the <path-realm>-realm resource-roles rather than "realm-management"
    const resource =
      keycloak.realm === "master" ? `${realm}-realm` : "realm-management";
    // console.log("using resource", resource);
    let roleAccess: boolean[] = [];
    requiredResourceRoles.map((role) => {
      return roleAccess.push(keycloak.hasResourceRole(role, resource));
    });

    setHasAccess(!roleAccess.includes(false));
  }, [keycloak?.token]);

  useEffect(() => {
    if (hasAccess === false && realm) {
      navigateToAccessDenied();
    }
  }, [hasAccess, realm]);

  return { hasAccess, navigateToAccessDenied };
}
