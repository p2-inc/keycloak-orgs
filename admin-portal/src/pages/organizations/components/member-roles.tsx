import { Menu } from "@headlessui/react";
import {
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  UserRepresentation,
} from "@/store/apis/orgs";
import { Roles, roleSettings } from "@/services/role";
import RoleBadge from "@/components/elements/badges/role-badge";
import { Link } from "react-router-dom";
import Button from "@/components/elements/forms/buttons/button";
import { checkOrgForRole } from "@/components/utils/check-org-for-role";
import useUser from "@/components/utils/useUser";
import { useTranslation } from "react-i18next";

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
  member: UserRepresentation;
  orgId: string;
};

const FilteredRole: React.FC<FilteredRoleProp> = ({
  regexp,
  regexpName,
  regexpClassName,
  roles,
  member,
  orgId,
}) => {
  const { hasManageRolesRole: hasManageRolesRoleCheck, user } = useUser();
  const filtered = roles.filter((f) => regexp.test(f.name));
  const hasManageRolesRole = hasManageRolesRoleCheck(orgId);
  const isSameUserAndMember = member.id === user?.id;
  const { t } = useTranslation();

  return (
    <Menu as="div" className="relative inline-block w-full text-left md:w-auto">
      <Menu.Button className="w-full">
        <div className="flex w-full items-center justify-center space-x-2 rounded border border-gray-200 py-1 px-4 text-sm transition hover:border-gray-800 dark:border-zinc-800 dark:hover:border-zinc-600">
          <span
            className={`inline-block h-2 w-2 rounded-full ${regexpClassName}`}
          ></span>
          <span className="inline-block dark:text-zinc-200">
            {filtered.length} {regexpName}
          </span>
        </div>
      </Menu.Button>
      <Menu.Items className="absolute right-0 z-10 mt-2 w-60 origin-top-right rounded-md bg-white p-4 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none dark:border dark:border-zinc-600 dark:bg-p2dark-900">
        {filtered.map((filteredRole) => (
          <Menu.Item key={filteredRole.name}>
            <div>
              <RoleBadge name={filteredRole.name} />
            </div>
          </Menu.Item>
        ))}
        {!isSameUserAndMember && hasManageRolesRole && (
          <Menu.Item>
            <Link to={`/organizations/${orgId}/members/${member.id}/roles`}>
              <Button isCompact className="mt-4 w-full">
                {t("editRoles")}
              </Button>
            </Link>
          </Menu.Item>
        )}
      </Menu.Items>
    </Menu>
  );
};

const MemberRoles: React.FC<Props> = ({ member, orgId, realm }) => {
  const { data: roles = [], isLoading } =
    useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
      orgId,
      realm,
      userId: member.id!,
    });

  const hasViewRolesRole = checkOrgForRole(roles, Roles.ViewRoles);

  return (
    <>
      {isLoading ? (
        <div className="inline-block h-[30px] w-32 animate-pulse rounded bg-gray-200"></div>
      ) : (
        hasViewRolesRole &&
        roleSettings.map((f) => (
          <FilteredRole
            regexp={f.regexp}
            regexpName={f.name}
            roles={roles}
            regexpClassName={f.className}
            key={f.name}
            member={member}
            orgId={orgId}
          />
        ))
      )}
    </>
  );
};

export default MemberRoles;
