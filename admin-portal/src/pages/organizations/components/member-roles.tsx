import SquareBadge from "components/elements/badges/square-badge";
import Button from "components/elements/forms/buttons/button";
import RolesLoader from "components/loaders/roles";
import { Menu } from "@headlessui/react";
import {
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  UserRepresentation,
} from "store/apis/orgs";
import { roleSettings } from "services/role";
import RoleBadge from "components/elements/badges/role-badge";

type Props = {
  member: UserRepresentation;
  orgId: string;
  realm: string;
};

type FilteredRoleProp = {
  regexp: RegExp;
  regexpName: string;
  regexpClassName: string;
  roles: Array<any>;
};

const FilteredRole: React.FC<FilteredRoleProp> = ({
  regexp,
  regexpName,
  regexpClassName,
  roles,
}) => {
  const filtered = roles.filter((f) => regexp.test(f.name));
  if (filtered.length > 0) {
    return (
      <Menu
        as="div"
        className="relative inline-block w-full text-left md:w-auto"
      >
        <Menu.Button className="w-full">
          <div className="flex w-full items-center justify-center space-x-2 rounded border border-gray-200 py-1 px-4 text-sm transition hover:border-gray-800">
            <span
              className={`inline-block h-2 w-2 rounded-full ${regexpClassName}`}
            ></span>
            <span className="inline-block">
              {filtered.length} {regexpName}
            </span>
          </div>
        </Menu.Button>
        <Menu.Items className="absolute right-0 z-10 w-60 p-4 mt-2 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
          {filtered.map((filteredRole) => (
            <Menu.Item>
              <div>
                <RoleBadge name={filteredRole.name} />
              </div>
            </Menu.Item>
          ))}
        </Menu.Items>
      </Menu>
    );
  } else {
    return <></>;
  }
};

const MemberRoles: React.FC<Props> = ({ member, orgId, realm }) => {
  const { data: roles = [], isLoading } =
    useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
      orgId,
      realm,
      userId: member.id!,
    });

  return (
    <>
      {isLoading && (
        <div className="h-[30px] w-32 animate-pulse rounded bg-gray-200 inline-block"></div>
      )}
      {roleSettings.map((f, key) => (
        <>
          {!isLoading && (
          <FilteredRole
            regexp={f.regexp}
            regexpName={f.name}
            roles={roles}
            regexpClassName={f.className}
          />)}
        </>
      ))}
    </>
  );
};

export default MemberRoles;
