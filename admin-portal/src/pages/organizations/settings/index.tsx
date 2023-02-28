import Button from "components/elements/forms/buttons/button";
import TopHeader from "components/navs/top-header";
import SecondaryMainContentMenuArea from "components/layouts/secondary-main-content-menu-area";
import FixedWidthMainContent from "components/layouts/fixed-width-main-content-area";
import PrimaryContentArea from "components/layouts/primary-content-area";
import SecondaryMainContentNav from "components/navs/secondary-main-content-nav";
import { Link, Outlet, useParams } from "react-router-dom";

const navigation = [
  {
    name: "General",
    href: "general",
  },
  {
    name: "Domains",
    href: "domains",
  },
  {
    name: "SSO",
    href: "sso",
  },
];

export default function OrganizationSettings() {
  let { orgId } = useParams();
  return (
    <>
      <TopHeader
        header="Organization Settings"
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
          <Outlet />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
