import Stat from "@/components/elements/cards/stat";
import { OrganizationRepresentation, useGetIdpsQuery } from "@/store/apis/orgs";
import { useTranslation } from "react-i18next";

type Props = {
  org: OrganizationRepresentation;
  realm: string;
};

const SsoConnections: React.FC<Props> = ({ org, realm }) => {
  const { t } = useTranslation();
  const { data: idps = [] } = useGetIdpsQuery({
    realm,
    orgId: org.id!,
  });

  const totalMembers = idps.length;

  return (
    <Stat value={totalMembers} label={t("org-details-stat-sso-connections")} />
  );
};

export default SsoConnections;
