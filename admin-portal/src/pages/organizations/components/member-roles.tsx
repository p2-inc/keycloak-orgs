import SquareBadge from "components/elements/badges/square-badge";
import RolesLoader from "components/loaders/roles";
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
  const { data: roles = [], isLoading } =
    useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
      orgId,
      realm,
      userId: member.id!,
    });

  return (
    <div className="flex flex-wrap justify-start ">
      {isLoading && <RolesLoader />}
      {roles.map((role) => (
        <SquareBadge key={role.name} className="mt-1 mr-1">
          {role.name}
        </SquareBadge>
      ))}
    </div>
  );
};

export default MemberRoles;
