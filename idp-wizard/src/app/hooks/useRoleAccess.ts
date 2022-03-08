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

  useEffect(() => {
    // token was authenticated or refreshed

    let roleAccess: boolean[] = [];
    requiredResourceRoles.map((role) =>
      roleAccess.push(keycloak.hasResourceRole(role, "realm-management"))
    );

    setHasAccess(roleAccess.includes(false));
  }, [keycloak?.token]);

  useEffect(() => {
    if (hasAccess === false && realm) {
      navigate(
        generatePath(PATHS.accessDenied, {
          realm,
        })
      );
    }
  }, [hasAccess, realm]);

  return [hasAccess];
}
