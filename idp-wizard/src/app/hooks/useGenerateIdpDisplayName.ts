import { useOrganization } from "./useOrganization";

export function useGenerateIdpDisplayName() {
  const { getCurrentOrgName } = useOrganization();

  function generateIdpDisplayName(alias: string): string {
    const orgName = getCurrentOrgName();
    if (!orgName) {
      return alias;
    }
    return `${orgName}-${alias}`;
  }

  return {
    generateIdpDisplayName,
  };
}
