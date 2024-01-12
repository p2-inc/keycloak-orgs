import SectionHeader from "components/navs/section-header";
import {
  useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery,
  useGetOrganizationMembershipsQuery,
  useGetOrganizationRolesQuery,
  useGrantUserOrganizationRoleMutation,
  useRevokeUserOrganizationRoleMutation,
} from "store/apis/orgs";
import { useState } from "react";
import { config } from "config";
import { Link, useParams } from "react-router-dom";
import P2Toast from "components/utils/toast";
import fullName from "components/utils/fullName";
import useUser from "components/utils/useUser";
import Alert from "components/elements/alerts/alert";
import {
  Roles as StandardRoles,
  OrgRoles as StandardOrgRoles,
} from "services/role";
import { useTranslation } from "react-i18next";
import { union } from "lodash";
import { Loader, LoadingIcon, SwitchItem, Button } from "./components";

const Roles = () => {
  const { t } = useTranslation();
  const {
    env: { realm },
  } = config;
  let { orgId, memberId } = useParams();
  const { user, hasManageRolesRole: hasManageRolesRoleCheck } = useUser();
  const [updatingRoles, setUpdatingRoles] = useState<string[]>([]);

  const { data: members = [] } = useGetOrganizationMembershipsQuery({
    orgId: orgId!,
    realm,
  });
  const currentMember = members.find((member) => member.id === memberId) || {};

  const {
    data: roles = [],
    isLoading,
    refetch: refetchRoles,
  } = useGetByRealmUsersAndUserIdOrgsOrgIdRolesQuery({
    orgId: orgId!,
    realm,
    userId: memberId!,
  });

  const { data: OrgRoles = [] } = useGetOrganizationRolesQuery({
    orgId: orgId!,
    realm,
  });

  const allRoles = union(
    StandardOrgRoles,
    OrgRoles?.map((or) => or.name)
  );

  const [grantUserOrganizationRole] = useGrantUserOrganizationRoleMutation();
  const [revokeUserOrganizationRole] = useRevokeUserOrganizationRoleMutation();

  const roleData: {
    name: string;
    isChecked: boolean;
    isApplicationRole: boolean;
  }[] = allRoles.sort().map((item) => {
    return {
      name: item,
      isChecked: roles.findIndex((f) => f.name === item) >= 0,
      isApplicationRole: !StandardOrgRoles.includes(item),
    };
  });

  const onRoleToggle = (roleName: string, checked: boolean) => {
    setUpdatingRoles([...updatingRoles, roleName]);
    if (checked) {
      grantUserOrganizationRole({
        name: roleName,
        orgId: orgId!,
        userId: memberId!,
        realm,
      })
        .unwrap()
        .then(() => {
          refetchRoles();
          P2Toast({
            success: true,
            title: t("role-toast-granted", { roleName }),
          });
        })
        .catch(() =>
          P2Toast({
            error: true,
            title: t("role-toast-granted-error", { roleName }),
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
        realm,
      })
        .unwrap()
        .then(() => {
          refetchRoles();
          P2Toast({
            success: true,
            title: t("role-toast-revoked", { roleName }),
          });
        })
        .catch(() =>
          P2Toast({
            error: true,
            title: t("role-toast-revoked-error", { roleName }),
          })
        )
        .finally(() => {
          setUpdatingRoles(updatingRoles.filter((r) => r !== roleName));
        });
    }
  };

  const grantAllRoles = () => {
    let grantRoles = roleData.filter((rd) => !rd.isChecked);
    setUpdatingRoles([...grantRoles.map((gr) => gr.name)]);
    Promise.all(
      grantRoles.map((ir) =>
        grantUserOrganizationRole({
          name: ir.name,
          orgId: orgId!,
          userId: memberId!,
          realm,
        })
      )
    )
      .then(() => {
        P2Toast({
          success: true,
          title: t("role-toast-granted-all"),
        });
      })
      .catch(() => {
        P2Toast({
          error: true,
          title: t("role-toast-granted-all-error"),
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
          realm,
        })
      )
    )
      .then(() => {
        P2Toast({
          success: true,
          title: t("role-toast-revoked-all"),
        });
      })
      .catch(() => {
        P2Toast({
          error: true,
          title: t("role-toast-revoked-all-error"),
        });
      })
      .finally(() => {
        setUpdatingRoles([]);
        refetchRoles();
      });
  };

  const grantFilteredRoles = (filter: "manage" | "view" | "application") => {
    const grantFilterRoles =
      filter === "application"
        ? roleData.filter((rd) => rd.isApplicationRole)
        : roleData.filter((rd) => rd.name.startsWith(filter));
    setUpdatingRoles([...grantFilterRoles.map((gr) => gr.name)]);
    Promise.all(
      grantFilterRoles.map((ir) =>
        grantUserOrganizationRole({
          name: ir.name,
          orgId: orgId!,
          userId: memberId!,
          realm,
        })
      )
    )
      .then(() => {
        P2Toast({
          success: true,
          title: `${t("role-toast-granted-filter")} ${t(filter)}.`,
        });
      })
      .catch(() => {
        P2Toast({
          error: true,
          title: t("role-toast-granted-error"),
        });
      })
      .finally(() => {
        setUpdatingRoles([]);
        refetchRoles();
      });
  };

  const isSameUserAndMember = currentMember.id === user?.id;

  const hasManageRolesRole = hasManageRolesRoleCheck(orgId);
  const hasApplicationRoles = OrgRoles.length > 0;

  return (
    <div className="mt-4 md:mt-16">
      <SectionHeader
        title={`${t("role-section-header-title")} ${fullName(currentMember)}`}
        icon={LoadingIcon}
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
            title={t("member-roles-missing-permission-title")}
            body={t("member-roles-missing-permission-body")}
            type="info"
          />
        </div>
      )}
      <div className="mt-8 flex items-center space-x-2 border-b pb-2">
        <div className="inline-block text-sm text-gray-600">{t("role-set")}</div>
        <Button
          onClick={grantAllRoles}
          disabled={
            !hasManageRolesRole ||
            roleData.filter((rd) => rd.isChecked).length === roleData.length
          }
          text={t("all")}
        />
        <Button
          onClick={() => grantFilteredRoles("manage")}
          disabled={
            !hasManageRolesRole ||
            !(
              roleData.filter(
                (rd) => rd.name.startsWith("manage") && !rd.isChecked
              ).length > 0
            )
          }
          text={t("allManage")}
        />
        <Button
          onClick={() => grantFilteredRoles("view")}
          disabled={
            !hasManageRolesRole ||
            !(
              roleData.filter(
                (rd) => rd.name.startsWith("view") && !rd.isChecked
              ).length > 0
            )
          }
          text={t("allView")}
        />
        {hasApplicationRoles && (
          <Button
            onClick={() => grantFilteredRoles("application")}
            disabled={
              !hasManageRolesRole ||
              !(
                roleData.filter((rd) => rd.isApplicationRole && !rd.isChecked)
                  .length > 0
              )
            }
            text={t("allApplication")}
          />
        )}
        <Button
          onClick={revokeAllRoles}
          disabled={
            !hasManageRolesRole ||
            roleData.filter((rd) => rd.isChecked).length === 0
          }
          text={t("none")}
        />
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
          ? allRoles.map((r) => <Loader key={r} />)
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
                roleType={
                  !item.isApplicationRole ? t("organization") : t("application")
                }
              />
            ))}
      </div>
    </div>
  );
};

export default Roles;
