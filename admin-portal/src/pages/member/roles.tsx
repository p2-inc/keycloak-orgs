import SectionHeader from "components/navs/section-header";
import cs from "classnames";
import Button from "components/elements/forms/buttons/button";
import {
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  useGetOrganizationMembershipsQuery,
  useGrantUserOrganizationRoleMutation,
  useRevokeUserOrganizationRoleMutation,
  useUpdateOrganizationRoleMutation,
} from "store/apis/orgs";
import { useState } from "react";
import { config } from "config";
import { Link, useParams } from "react-router-dom";
import { User } from "lucide-react";
import RoleBadge from "components/elements/badges/role-badge";
import { Switch } from "@headlessui/react";
import { defaultRoles } from "pages/invitation/new";
import P2Toast from "components/utils/toast";
import fullName from "components/utils/fullName";
import useUser from "components/utils/useUser";
import Alert from "components/elements/alerts/alert";

const loadingIcon = (
  <div>
    <div className={cs("relative h-12 w-12 overflow-hidden rounded-md")}>
      <div className="absolute -inset-10 z-10 bg-gradient-to-tr from-[#C7DFF0] to-[#1476B7]"></div>
      <div className="absolute inset-[2px] z-20 flex items-center justify-center rounded bg-white dark:bg-p2dark-1000 dark:text-zinc-200">
        <User />
      </div>
    </div>
  </div>
);

const Loader = () => {
  return (
    <div className="flex justify-between space-x-2 py-3">
      <div className="flex space-x-2">
        <div className="animate-pulse">
          <div className="h-4 w-4 rounded-md bg-gray-300"></div>
        </div>
        <div className="animate-pulse">
          <div className="h-4 w-20 rounded-md bg-gray-300"></div>
        </div>
      </div>
      <div className="animate-pulse">
        <div className="h-4 w-10 rounded-md bg-gray-300"></div>
      </div>
    </div>
  );
};

const SwitchItem = ({
  name,
  isChecked,
  onChange,
  isDisabled,
}: {
  name: string;
  isChecked: boolean;
  onChange: (roleName, checked) => void;
  isDisabled: boolean;
}) => {
  return (
    <Switch.Group>
      <div className="flex items-center justify-between py-2">
        <Switch.Label className="mr-4">
          <RoleBadge name={name} />
        </Switch.Label>
        <Switch
          checked={isChecked}
          disabled={isDisabled}
          onChange={(checked) => onChange(name, checked)}
          className={`${
            isChecked ? "bg-p2blue-500" : "bg-gray-200"
          } relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-p2blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50`}
        >
          <span
            className={`${
              isChecked ? "translate-x-6" : "translate-x-1"
            } inline-block h-4 w-4 transform rounded-full bg-white transition-transform`}
          />
        </Switch>
      </div>
    </Switch.Group>
  );
};

const Roles = () => {
  let { orgId, memberId } = useParams();
  const { user } = useUser();
  const [updatingRoles, setUpdatingRoles] = useState<string[]>([]);

  const { data: members = [] } = useGetOrganizationMembershipsQuery({
    orgId: orgId!,
    realm: config.env.realm,
  });
  const currentMember = members.find((member) => member.id === memberId) || {};

  const {
    data: roles = [],
    isLoading,
    refetch: refetchRoles,
  } = useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
    orgId: orgId!,
    realm: config.env.realm,
    userId: memberId!,
  });

  const [grantUserOrganizationRole] = useGrantUserOrganizationRoleMutation();
  const [revokeUserOrganizationRole] = useRevokeUserOrganizationRoleMutation();

  const onRoleToggle = (roleName: string, checked: boolean) => {
    setUpdatingRoles([...updatingRoles, roleName]);
    if (checked) {
      grantUserOrganizationRole({
        name: roleName,
        orgId: orgId!,
        userId: memberId!,
        realm: config.env.realm,
      })
        .unwrap()
        .then(() => {
          refetchRoles();
          P2Toast({
            success: true,
            title: `Granted ${roleName} role to user.`,
          });
        })
        .catch(() =>
          P2Toast({
            error: true,
            title: `Error granting ${roleName} role to user. Please try again.`,
          })
        )
        .finally(() => {
          setUpdatingRoles(updatingRoles.filter((r) => r !== roleName));
        });
    } else {
      revokeUserOrganizationRole({
        name: roleName,
        orgId: orgId!,
        userId: memberId!,
        realm: config.env.realm,
      })
        .unwrap()
        .then(() => {
          refetchRoles();
          P2Toast({
            success: true,
            title: `Revoked ${roleName} role user.`,
          });
        })
        .catch(() =>
          P2Toast({
            error: true,
            title: `Error revoking ${roleName} role to user. Please try again.`,
          })
        )
        .finally(() => {
          setUpdatingRoles(updatingRoles.filter((r) => r !== roleName));
        });
    }
  };

  const roleData = Array.from(defaultRoles)
    .sort()
    .map((item) => {
      return {
        name: item,
        isChecked: roles.findIndex((f) => f.name === item) >= 0,
      };
    });

  const isSameUserAndMember = currentMember.id === user?.id;

  const doesNotHaveManageRole =
    roleData.find((rd) => rd.name === "manage-roles")?.isChecked === false;

  return (
    <div className="mt-4 md:mt-16">
      <SectionHeader
        title={`Edit ${fullName(currentMember) || "member"}'s roles`}
        icon={loadingIcon}
        rightContent={
          <Link
            to={`/organizations/${orgId}/details`}
            className="inline-block rounded-lg px-4 py-2 font-medium opacity-60 transition hover:bg-gray-100 hover:opacity-100 dark:text-zinc-200 dark:hover:bg-p2dark-1000"
          >
            Back
          </Link>
        }
      />
      {isSameUserAndMember && (
        <div className="mt-4">
          <Alert
            title="You are the same user as this user."
            body="Any changes you make will have immediate effect."
            type="danger"
          />
        </div>
      )}
      {isSameUserAndMember && doesNotHaveManageRole && (
        <div className="mt-4">
          <Alert
            title='You lack the "manage-roles" role.'
            body="Speak to an admin in order to be granted this role."
            type="info"
          />
        </div>
      )}
      <div className="mt-8 divide-y">
        {isLoading
          ? Array.from(defaultRoles).map((r) => <Loader />)
          : roleData.map((item) => (
              <SwitchItem
                name={item.name}
                isChecked={item.isChecked}
                onChange={onRoleToggle}
                isDisabled={
                  (isSameUserAndMember && doesNotHaveManageRole) ||
                  updatingRoles.includes(item.name)
                }
              />
            ))}
      </div>
    </div>
  );
};

export default Roles;
