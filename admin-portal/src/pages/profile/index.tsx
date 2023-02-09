import SecondaryMainContentMenuArea from "components/layouts/secondary-main-content-menu-area";
import TopHeader from "components/navs/top-header";
import cs from "classnames";
import {
  KeyIcon,
  SquaresPlusIcon,
  UserCircleIcon,
  DevicePhoneMobileIcon,
} from "@heroicons/react/24/outline";
import { NavLink } from "react-router-dom";
import FixedWidthMainContent from "components/layouts/main-content-area copy";
import PrimaryContentArea from "components/layouts/primary-content-area";
import GeneralProfile from "./general";
import SecondaryMainContentNav from "components/navs/secondary-main-content-nav";

const navigation = [
  {
    name: "General",
    href: "/profile/general",
    icon: UserCircleIcon,
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
          <GeneralProfile />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
