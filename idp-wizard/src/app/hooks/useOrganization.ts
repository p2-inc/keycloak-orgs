import { useKeycloak } from "@react-keycloak/web";
import { useAppSelector } from "./hooks";

export function useOrganization() {
  const { keycloak } = useKeycloak();
  const orgs = keycloak?.tokenParsed?.organizations || {};
  const currentOrg = useAppSelector((state) => state.settings.currentOrg);

  function getCurrentOrg() {
    return orgs[currentOrg!];
  }
  function getCurrentOrgName() {
    if (currentOrg === "global") {
      return "Global";
    }
    return orgs[currentOrg!]?.name;
  }

  return { currentOrg, getCurrentOrg, getCurrentOrgName };
}
