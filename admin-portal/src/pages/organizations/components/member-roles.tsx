import SquareBadge from "components/elements/badges/square-badge";
import {
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  UserRepresentation,
} from "store/apis/orgs";

type Props = {
  member: UserRepresentation;
  orgId: string;
  realm: string;
};

const MemberRoles: React.FC<Props> = ({ member, orgId, realm }) => {
  const { data: roles = [] } = useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
    orgId,
    realm,
    userId: member.id!,
  });

  return (
    <div className="space-x-2 space-y-2">
      {roles.map((role) => (
        <SquareBadge>{role.name}</SquareBadge>
      ))}
    </div>
  );
};

export default MemberRoles;
