import SecondaryMainContentMenuArea from "components/layouts/secondary-main-content-menu-area";
import TopHeader from "components/navs/top-header";
import {
  KeyIcon,
  SquaresPlusIcon,
  UserCircleIcon,
  DevicePhoneMobileIcon,
} from "@heroicons/react/24/outline";
import FixedWidthMainContent from "components/layouts/fixed-width-main-content-area";
import PrimaryContentArea from "components/layouts/primary-content-area";
import SecondaryMainContentNav, {
  NavigationItem,
} from "components/navs/secondary-main-content-nav";
import { Outlet } from "react-router-dom";
import { config } from "config";

const navigation: NavigationItem[] = [];

const addToNavigation = (data: NavigationItem, condition: boolean) => {
  if (condition) {
    navigation.push(data);
  }
};
const featureFlags = config.env.features;
addToNavigation(
  {
    name: "General",
    href: "/profile/general",
    icon: UserCircleIcon,
  },
  true
);
addToNavigation(
  {
    name: "Signing in",
    href: "/profile/signin",
    icon: KeyIcon,
  },
  true
);
addToNavigation(
  {
    name: "Device activity",
    href: "/profile/activity",
    icon: DevicePhoneMobileIcon,
  },
  featureFlags.deviceActivityEnabled
);
addToNavigation(
  {
    name: "Linked accounts",
    href: "/profile/linked",
    icon: SquaresPlusIcon,
  },
  featureFlags.linkedAccountsEnabled
);

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
