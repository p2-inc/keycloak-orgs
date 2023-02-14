import SecondaryMainContentMenuArea from "components/layouts/secondary-main-content-menu-area";
import TopHeader from "components/navs/top-header";
import {
  KeyIcon,
  SquaresPlusIcon,
  UserCircleIcon,
  DevicePhoneMobileIcon,
} from "@heroicons/react/24/outline";
import FixedWidthMainContent from "components/layouts/main-content-area copy";
import PrimaryContentArea from "components/layouts/primary-content-area";
import SecondaryMainContentNav from "components/navs/secondary-main-content-nav";
import { Outlet } from "react-router-dom";

const navigation = [
  {
    name: "General",
    href: "/profile/general",
    icon: UserCircleIcon,
  },
  {
    name: "Role",
    href: "/profile/role",
    icon: KeyIcon,
  },
  {
    name: "Signing in",
    href: "/profile/signin",
    icon: KeyIcon,
  },
  {
    name: "Device activity",
    href: "/profile/activity",
    icon: DevicePhoneMobileIcon,
  },
  {
    name: "Linked accounts",
    href: "/profile/linked",
    icon: SquaresPlusIcon,
  },
];

export default function Profile() {
  return (
    <>
      <TopHeader header="Profile" />
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
