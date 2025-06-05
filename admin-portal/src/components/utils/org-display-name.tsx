import { useTranslation } from "react-i18next";
import { OrganizationRepresentation } from "@/store/apis/orgs";

function useOrgDisplayName(org?: OrganizationRepresentation) {
  const { t } = useTranslation();
  const orgName = org?.displayName || org?.name || t("organization");

  return { orgName };
}

export default useOrgDisplayName;
