import { Fragment, useState } from "react";
import { Menu, Transition } from "@headlessui/react";
import { EllipsisVerticalIcon } from "@heroicons/react/20/solid";
import {
  useRemoveOrganizationMemberMutation,
  UserRepresentation,
} from "store/apis/orgs";
import { KeycloakProfile } from "keycloak-js";
import MemberRemovalConfirmationDialog from "./member-removal-confirmation-dialog";
import P2Toast from "components/utils/toast";
import useUser from "components/utils/useUser";
import fullName from "components/utils/fullName";
import { Link } from "react-router-dom";
import MenuItemButton from "components/elements/menu/button";
import { useTranslation } from "react-i18next";

type Props = {
  member: UserRepresentation;
  user?: KeycloakProfile;
  orgId: string;
  realm: string;
};

export default function MembersActionMenu({ member, orgId, realm }: Props) {
  const { t } = useTranslation();
  const {
    user,
    hasManageMembersRole: hasManageMembersRoleCheck,
    hasManageRolesRole: hasManageRolesRoleCheck,
  } = useUser();
  const isSameUserAndMember = member.id === user?.id;
  const isRemoveDisabled = !user || isSameUserAndMember;

  const [isRemoveConfOpen, setRemoveConfOpen] = useState(false);

  const [removeOrganizationMember, { isLoading }] =
    useRemoveOrganizationMemberMutation();

  function confirmRemoveOrganizationMember() {
    removeOrganizationMember({
      orgId,
      realm,
      userId: member.id!,
    })
      .then(() => {
        P2Toast({
          success: true,
          title: `${fullName(member)} removed from organization.`,
        });
        setRemoveConfOpen(false);
      })
      .catch((e) => {
        P2Toast({
          error: true,
          title: "Error during removal. Please try again.",
        });
        console.error(e);
      });
  }

  const hasManageMembersRole = hasManageMembersRoleCheck(orgId);
  const hasManageRolesRole = hasManageRolesRoleCheck(orgId);

  return (
    <>
      <MemberRemovalConfirmationDialog
        open={isRemoveConfOpen}
        setOpen={setRemoveConfOpen}
        confirmSelection={confirmRemoveOrganizationMember}
        member={member}
        isLoading={isLoading}
      />

      <Menu
        as="div"
        className="relative inline-block w-full text-left md:w-auto"
      >
        <div className="flex h-[40px] items-center">
          <Menu.Button className="w-full">
            <div className="flex w-full items-center justify-center space-x-2 rounded border border-gray-200 py-1 px-4 text-sm transition hover:border-gray-800 md:border-transparent md:px-1 dark:md:border-zinc-800 dark:md:hover:border-zinc-600">
              <EllipsisVerticalIcon
                className="h-5 w-5 dark:fill-zinc-200 max-md:hidden"
                aria-hidden="true"
              />
              <span className="md:hidden">{t("options")}</span>
            </div>
          </Menu.Button>
        </div>

        <Transition
          as={Fragment}
          enter="transition ease-out duration-100"
          enterFrom="transform opacity-0 scale-95"
          enterTo="transform opacity-100 scale-100"
          leave="transition ease-in duration-75"
          leaveFrom="transform opacity-100 scale-100"
          leaveTo="transform opacity-0 scale-95"
        >
          <Menu.Items className="absolute right-0 z-10 mt-2 w-56 origin-top-right rounded-md bg-white shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none dark:border dark:border-zinc-600 dark:bg-p2dark-900">
            <div className="py-1">
              <Menu.Item disabled={isSameUserAndMember || !hasManageRolesRole}>
                {({ active, disabled }) => {
                  return disabled ? (
                    <MenuItemButton active={active} disabled={disabled}>
                      {t("editRoles")}
                    </MenuItemButton>
                  ) : (
                    <Link
                      to={`/organizations/${orgId}/members/${member.id}/roles`}
                    >
                      <MenuItemButton active={active} disabled={disabled}>
                        {t("editRoles")}
                      </MenuItemButton>
                    </Link>
                  );
                }}
              </Menu.Item>
              <Menu.Item disabled={isRemoveDisabled || !hasManageMembersRole}>
                {({ active, disabled }) => {
                  return (
                    <MenuItemButton
                      onClick={() => setRemoveConfOpen(true)}
                      active={active}
                      disabled={disabled}
                    >
                      {t("remove")}
                    </MenuItemButton>
                  );
                }}
              </Menu.Item>
            </div>
          </Menu.Items>
        </Transition>
      </Menu>
    </>
  );
}
