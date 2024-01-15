import Stat from "components/elements/cards/stat";
import {
  OrganizationRepresentation,
  useGetOrganizationMembershipsQuery,
} from "store/apis/orgs";
import {useTranslation} from "react-i18next";

type Props = {
  org: OrganizationRepresentation;
  realm: string;
};

const MembersStat: React.FC<Props> = ({ org, realm }) => {
  const { t } = useTranslation();
  const { data: members = [] } = useGetOrganizationMembershipsQuery({
    realm: realm,
    orgId: org.id!,
  });

  const totalMembers = members.length === 0 ? 0 : members.length - 1;

  return <Stat value={totalMembers} label={t("members")} />;
};

export default MembersStat;
