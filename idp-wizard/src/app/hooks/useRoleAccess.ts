import { generateBaseRealmPath } from "@app/routes";
import { useKeycloak } from "@react-keycloak/web";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

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
  let navigate = useNavigate();
  const accessDenied = `${generateBaseRealmPath()}access-denied`;
  // Starts as null to make true/false explicit states
  const [hasAccess, setHasAccess] = useState<null | boolean>(null);

  useEffect(() => {
    // token was authenticated or refreshed

    let roleAccess: boolean[] = [];
    requiredResourceRoles.map((role) =>
      roleAccess.push(keycloak.hasResourceRole(role, "realm-management"))
    );

    if (roleAccess.includes(false)) {
      setHasAccess(false);

      // Or choose to navigate away
      // navigate(accessDenied);
    } else {
      setHasAccess(true);
    }
  }, [keycloak?.token]);

  return [hasAccess];
}
