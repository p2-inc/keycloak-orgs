import Button from "components/elements/forms/buttons/button";
import TopHeader from "components/navs/top-header";
import SecondaryMainContentMenuArea from "components/layouts/secondary-main-content-menu-area";
import FixedWidthMainContent from "components/layouts/fixed-width-main-content-area";
import PrimaryContentArea from "components/layouts/primary-content-area";
import SecondaryMainContentNav from "components/navs/secondary-main-content-nav";
import { apiRealm } from "store/apis/helpers";
import { Link, Outlet, useParams } from "react-router-dom";
import Breadcrumbs from "components/navs/breadcrumbs";
import { useGetOrganizationByIdQuery } from "store/apis/orgs";
import SettingsGeneral from "./general";
import SettingsDomain from "./domains";
import SettingsSSO from "./sso";

const navigation = [
  {
    name: "General",
    href: "#general",
  },
  {
    name: "Domains",
    href: "#domains",
  },
  {
    name: "SSO",
    href: "#sso",
  },
];

export default function OrganizationSettings() {
  let { orgId } = useParams();
  const { data: org } = useGetOrganizationByIdQuery({
    orgId: orgId!,
    realm: apiRealm,
  });
  return (
    <>
      <TopHeader
        header="Settings"
        collapseOnMobile={true}
        leftAreaItems={
          <Breadcrumbs
            items={[
              { title: "Organizations", link: `/organizations` },
              {
                title: `${org?.displayName || "Organization"}`.trim(),
                link: `/organizations/${orgId}/details`,
              },
            ]}
          />
        }
        rightAreaItems={
          <>
            <Link to={`/organizations/${orgId}/details`}>
              <Button>Back to Org</Button>
            </Link>
          </>
        }
      />
      <FixedWidthMainContent>
        {/* Secondary menu */}
        <SecondaryMainContentMenuArea>
          <SecondaryMainContentNav navigation={navigation} />
        </SecondaryMainContentMenuArea>

        {/* Primary content */}
        <PrimaryContentArea>
          <SettingsGeneral />
          <hr className="my-10" />
          <SettingsDomain />
          <hr className="my-10" />
          <SettingsSSO />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
