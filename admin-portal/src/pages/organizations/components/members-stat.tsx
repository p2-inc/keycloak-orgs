import Stat from "components/elements/cards/stat";
import {
  OrganizationRepresentation,
  useGetOrganizationMembershipsQuery,
} from "store/apis/orgs";

type Props = {
  org: OrganizationRepresentation;
  realm: string;
};

const MembersStat: React.FC<Props> = ({ org, realm }) => {
  const { data: members = [] } = useGetOrganizationMembershipsQuery({
    realm: realm,
    orgId: org.id!,
  });

  return <Stat value={members.length} label="members" />;
};

export default MembersStat;
