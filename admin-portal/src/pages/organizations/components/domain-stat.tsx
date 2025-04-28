import Stat from "components/elements/cards/stat";
import {
  OrganizationRepresentation,
  useGetOrganizationDomainsQuery,
} from "store/apis/orgs";
import { useTranslation } from "react-i18next";

type Props = {
  org: OrganizationRepresentation;
  realm: string;
};

const DomainStat: React.FC<Props> = ({ org, realm }) => {
  const { t } = useTranslation();
  const { data: domains = [] } = useGetOrganizationDomainsQuery({
    realm,
    orgId: org.id!,
  });

  const verifiedPercentage =
    domains.length > 0
      ? (100 * domains.filter((d) => d.verified).length) / domains.length
      : 0;

  return (
    <Stat
      percent={domains.length === 0 ? null : verifiedPercentage}
      hoverPercentText="Percentage of domains verified"
      value={org.domains?.length || "0"}
      label={t("domains")}
    />
  );
};

export default DomainStat;
