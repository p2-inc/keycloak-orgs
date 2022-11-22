import { useKeycloak } from "@react-keycloak/web";
import { useAppSelector } from "./hooks";

export function useOrganization() {
  const { keycloak } = useKeycloak();
  const orgs = keycloak?.tokenParsed?.organizations;
  const currentOrg = useAppSelector((state) => state.settings.selectedOrg);

  function getSelectedOrg() {
    return orgs[currentOrg!];
  }
  function getSelectedOrgName() {
    return orgs[currentOrg!]?.name;
  }

  return { getSelectedOrg, getSelectedOrgName };
}
