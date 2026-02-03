import { config } from "@/config";
import { sortBy } from "lodash";
import {
  OrgRoles as StandardOrgRoles,
  Roles as StandardRoles,
} from "@/services/role";
import { useGetOrganizationRolesQuery } from "@/store/apis/orgs";
import { Loader } from "./loader";
import { SwitchItem } from "./switch";
import { useTranslation } from "react-i18next";
import { useEffect, useState } from "react";
import { Button } from "./button";

const { realm } = config.env;

export type DecoratedRole = {
  name: string;
  isChecked: boolean;
  isApplicationRole: boolean;
};

export const RolesList = ({
  orgId,
  setSelectedRoles,
}: {
  orgId: string;
  setSelectedRoles: (roles: DecoratedRole[]) => void;
}) => {
  const { t } = useTranslation();
  const [roles, setRoles] = useState<DecoratedRole[]>([]);

  const { data: OrgRoles = [], isLoading } = useGetOrganizationRolesQuery({
    orgId: orgId!,
    realm,
  });

  useEffect(() => {
    const decoratedRoles = sortBy(
      OrgRoles.map((r) => {
        const isStandardRole = StandardOrgRoles.includes(
          r.name as StandardRoles
        );
        return {
          name: r.name!,
          isChecked: isStandardRole,
          isApplicationRole: !isStandardRole,
        };
      }),
      ["isApplicationRole", "name"]
    );
    setRoles([...decoratedRoles]);
  }, [isLoading]);

  useEffect(() => {
    setSelectedRoles(roles);
  }, [roles]);

  const hasApplicationRoles =
    roles.filter((r) => r.isApplicationRole).length > 0;

  return (
    <div>
      {isLoading ? (
        [1, 2, 3].map((r) => <Loader key={r} />)
      ) : (
        <>
          <div className="mt-8 flex items-center space-x-2 border-b pb-2">
            <div className="inline-block text-sm text-gray-600 dark:text-zinc-300">
              Choose roles:
            </div>
            <Button
              onClick={() =>
                setRoles(roles.map((r) => ({ ...r, isChecked: true })))
              }
              disabled={
                roles.filter((rd) => rd.isChecked).length === roles.length
              }
              text={t("all")}
            />
            <Button
              onClick={() =>
                setRoles(
                  roles.map((r) => ({
                    ...r,
                    isChecked: r.isChecked || r.name.startsWith("manage"),
                  }))
                )
              }
              disabled={
                !(
                  roles.filter(
                    (rd) => rd.name.startsWith("manage") && !rd.isChecked
                  ).length > 0
                )
              }
              text={t("allManage")}
            />
            <Button
              onClick={() =>
                setRoles(
                  roles.map((r) => ({
                    ...r,
                    isChecked: r.isChecked || r.name.startsWith("view"),
                  }))
                )
              }
              disabled={
                !(
                  roles.filter(
                    (rd) => rd.name.startsWith("view") && !rd.isChecked
                  ).length > 0
                )
              }
              text={t("allView")}
            />
            {hasApplicationRoles && (
              <Button
                onClick={() =>
                  setRoles(
                    roles.map((r) => ({
                      ...r,
                      isChecked: r.isChecked || r.isApplicationRole,
                    }))
                  )
                }
                disabled={
                  !(
                    roles.filter((rd) => rd.isApplicationRole && !rd.isChecked)
                      .length > 0
                  )
                }
                text={t("allApplication")}
              />
            )}
            <Button
              onClick={() =>
                setRoles(
                  roles.map((r) => ({
                    ...r,
                    isChecked: false,
                  }))
                )
              }
              disabled={roles.filter((rd) => rd.isChecked).length === 0}
              text={t("none")}
            />
          </div>
          <div>
            {roles.map((item) => (
              <SwitchItem
                name={item.name}
                isChecked={item.isChecked}
                onChange={() =>
                  setRoles([
                    ...roles.map((r) => ({
                      ...r,
                      isChecked:
                        item.name === r.name ? !r.isChecked : r.isChecked,
                    })),
                  ])
                }
                key={item.name}
                roleType={
                  !item.isApplicationRole ? t("organization") : t("application")
                }
              />
            ))}
          </div>
        </>
      )}
    </div>
  );
};
