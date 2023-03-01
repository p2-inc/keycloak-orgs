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
        header="Settings"
        collapseOnMobile={true}
        leftAreaItems={
          <div className="mr-2 flex items-center space-x-2">
            <Link
              to={`/organizations/${orgId}/details`}
              className="-ml-3 -mr-3 rounded-lg px-3 py-1 font-medium transition hover:bg-gray-100 md:text-xl"
            >
              <div>Organization</div>
            </Link>
            <div className="hidden text-xl opacity-20 md:block">/</div>
          </div>
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
          <Outlet />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
