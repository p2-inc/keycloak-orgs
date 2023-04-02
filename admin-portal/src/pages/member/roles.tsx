import SectionHeader from "components/navs/section-header";
import cs from "classnames";
import {
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  useGetOrganizationMembershipsQuery,
  useGrantUserOrganizationRoleMutation,
  useRevokeUserOrganizationRoleMutation,
} from "store/apis/orgs";
import { useState } from "react";
import { config } from "config";
import { Link, useParams } from "react-router-dom";
import { User } from "lucide-react";
import RoleBadge from "components/elements/badges/role-badge";
import { Switch } from "@headlessui/react";
import P2Toast from "components/utils/toast";
import fullName from "components/utils/fullName";
import useUser from "components/utils/useUser";
import Alert from "components/elements/alerts/alert";
import { OrgRoles } from "services/role";
import { useTranslation } from "react-i18next";

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

const buttonClasses =
  "rounded bg-indigo-50 py-1 px-2 text-xs font-semibold text-p2blue-700 shadow-sm enabled:hover:bg-indigo-100 disabled:opacity-50";

const Roles = () => {
  const { t } = useTranslation();
  let { orgId, memberId } = useParams();
  const { user, hasManageRolesRole: hasManageRolesRoleCheck } = useUser();
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

  const roleData = OrgRoles.sort().map((item) => {
    return {
      name: item,
      isChecked: roles.findIndex((f) => f.name === item) >= 0,
    };
  });

  const grantAllRoles = () => {
    let grantRoles = roleData.filter((rd) => !rd.isChecked);
    setUpdatingRoles([...grantRoles.map((gr) => gr.name)]);
    Promise.all(
      grantRoles.map((ir) =>
        grantUserOrganizationRole({
          name: ir.name,
          orgId: orgId!,
          userId: memberId!,
          realm: config.env.realm,
        })
      )
    )
      .then(() => {
        P2Toast({
          success: true,
          title: `Granted all roles to user.`,
        });
      })
      .catch(() => {
        P2Toast({
          error: true,
          title: `Error granting all roles to user. Please try again.`,
        });
      })
      .finally(() => {
        setUpdatingRoles([]);
        refetchRoles();
      });
  };

  const revokeAllRoles = () => {
    const revokeRoles = roleData.filter((rd) => rd.isChecked);
    setUpdatingRoles([...revokeRoles.map((gr) => gr.name)]);
    Promise.all(
      revokeRoles.map((ir) =>
        revokeUserOrganizationRole({
          name: ir.name,
          orgId: orgId!,
          userId: memberId!,
          realm: config.env.realm,
        })
      )
    )
      .then(() => {
        P2Toast({
          success: true,
          title: `Revoked all roles for user.`,
        });
      })
      .catch(() => {
        P2Toast({
          error: true,
          title: `Error revoking all roles for user. Please try again.`,
        });
      })
      .finally(() => {
        setUpdatingRoles([]);
        refetchRoles();
      });
  };

  const grantFilteredRoles = (filter: "manage" | "view") => {
    const grantFilterRoles = roleData.filter((rd) =>
      rd.name.startsWith(filter)
    );
    setUpdatingRoles([...grantFilterRoles.map((gr) => gr.name)]);
    Promise.all(
      grantFilterRoles.map((ir) =>
        grantUserOrganizationRole({
          name: ir.name,
          orgId: orgId!,
          userId: memberId!,
          realm: config.env.realm,
        })
      )
    )
      .then(() => {
        P2Toast({
          success: true,
          title: `Granted ${filter} roles to user.`,
        });
      })
      .catch(() => {
        P2Toast({
          error: true,
          title: `Error granting ${filter} roles to user. Please try again.`,
        });
      })
      .finally(() => {
        setUpdatingRoles([]);
        refetchRoles();
      });
  };

  const isSameUserAndMember = currentMember.id === user?.id;

  const hasManageRolesRole = hasManageRolesRoleCheck(orgId);

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
      {!hasManageRolesRole && (
        <div className="mt-4">
          <Alert
            title='You lack the "manage-roles" role.'
            body="Speak to an admin in order to be granted this role."
            type="info"
          />
        </div>
      )}
      <div className="mt-8 flex items-center space-x-2 border-b pb-2">
        <div className="inline-block text-sm text-gray-600">Set roles:</div>
        <button
          className={buttonClasses}
          onClick={grantAllRoles}
          disabled={
            !hasManageRolesRole ||
            roleData.filter((rd) => rd.isChecked).length === roleData.length
          }
        >
          all
        </button>
        <button
          className={buttonClasses}
          onClick={() => grantFilteredRoles("manage")}
          disabled={
            !hasManageRolesRole ||
            !(
              roleData.filter(
                (rd) => rd.name.startsWith("manage") && !rd.isChecked
              ).length > 0
            )
          }
        >
          all manage
        </button>
        <button
          className={buttonClasses}
          onClick={() => grantFilteredRoles("view")}
          disabled={
            !hasManageRolesRole ||
            !(
              roleData.filter(
                (rd) => rd.name.startsWith("view") && !rd.isChecked
              ).length > 0
            )
          }
        >
          all view
        </button>
        <button
          className={buttonClasses}
          onClick={revokeAllRoles}
          disabled={
            !hasManageRolesRole ||
            roleData.filter((rd) => rd.isChecked).length === 0
          }
        >
          none
        </button>
      </div>
      {isSameUserAndMember && (
        <div className="mt-4">
          <Alert
            title={t("youAreTheSameUserAsThisUser")}
            body={t("anyChangesYouMakeWillHaveImmediateEffect")}
            type="danger"
          />
        </div>
      )}
      {isSameUserAndMember && !hasManageRolesRole && (
        <div className="mt-4">
          <Alert
            title={t("youLackTheManageRolesRole")}
            body={t("speakToAnAdminInOrderToBeGrantedThisRole")}
            type="info"
          />
        </div>
      )}
      <div className="divide-y dark:divide-zinc-600">
        {isLoading
          ? OrgRoles.map((r) => <Loader key={r} />)
          : roleData.map((item) => (
              <SwitchItem
                name={item.name}
                isChecked={item.isChecked}
                onChange={onRoleToggle}
                isDisabled={
                  isSameUserAndMember ||
                  !hasManageRolesRole ||
                  updatingRoles.includes(item.name)
                }
                key={item.name}
              />
            ))}
      </div>
    </div>
  );
};

export default Roles;
