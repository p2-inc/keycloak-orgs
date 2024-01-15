import Button from "components/elements/forms/buttons/button";
import TopHeader from "components/navs/top-header";
import FixedWidthMainContent from "components/layouts/fixed-width-main-content-area";
import PrimaryContentArea from "components/layouts/primary-content-area";
import { config } from "config";
import { Link, useParams } from "react-router-dom";
import Breadcrumbs from "components/navs/breadcrumbs";
import { useGetOrganizationByIdQuery } from "store/apis/orgs";
import SettingsGeneral from "./general";
import SettingsDomain from "./domains";
import SettingsSSO from "./sso";
import useUser from "components/utils/useUser";
import { useTranslation } from "react-i18next";

export type SettingsProps = {
  hasManageOrganizationRole?: boolean;
  hasManageIDPRole?: boolean;
};

export default function OrganizationSettings() {
  const { t } = useTranslation();
  let { orgId } = useParams();
  const {
    hasManageOrganizationRole: hasManageOrganizationRoleCheck,
    hasManageIdentityProvidersRole,
  } = useUser();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: config.env.realm,
  });
  const hasManageOrganizationRole = hasManageOrganizationRoleCheck(orgId);
  const hasManageIDPRole = hasManageIdentityProvidersRole(orgId);
  return (
    <>
      <TopHeader
        header={t("settings")}
        collapseOnMobile={true}
        leftAreaItems={
          <Breadcrumbs
            items={[
              { title: t("organizations"), link: `/organizations` },
              {
                title: `${
                  org?.displayName || org?.name || t("organization")
                }`.trim(),
                link: `/organizations/${orgId}/details`,
              },
            ]}
          />
        }
        rightAreaItems={
          <>
            <Link to={`/organizations/${orgId}/details`}>
              <Button>{t("backToOrg")}</Button>
            </Link>
          </>
        }
      />
      <FixedWidthMainContent>
        <PrimaryContentArea>
          <SettingsGeneral
            hasManageOrganizationRole={hasManageOrganizationRole}
          />
          <hr className="my-10 dark:border-zinc-600" />
          <SettingsDomain
            hasManageOrganizationRole={hasManageOrganizationRole}
          />
          <hr className="my-10 dark:border-zinc-600" />
          <SettingsSSO hasManageIDPRole={hasManageIDPRole} />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
