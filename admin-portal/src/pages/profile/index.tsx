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
import { useTranslation } from "react-i18next";
const { features: featureFlags } = config.env;

const navigation: NavigationItem[] = [
  {
    name: "general",
    href: "/profile/general",
    icon: UserCircleIcon,
    isActive: true,
  },
  {
    name: "signingIn",
    href: "/profile/signin",
    icon: KeyIcon,
    isActive: true,
  },
  {
    name: "deviceActivity",
    href: "/profile/activity",
    icon: DevicePhoneMobileIcon,
    isActive: featureFlags.deviceActivityEnabled,
  },
  {
    name: "linkedAccounts",
    href: "/profile/linked",
    icon: SquaresPlusIcon,
    isActive: featureFlags.linkedAccountsEnabled,
  },
];

export default function Profile() {
  const { t } = useTranslation();
  return (
    <>
      <TopHeader header={t("profile")} />
      <FixedWidthMainContent>
        {/* Secondary menu */}
        <SecondaryMainContentMenuArea>
          <SecondaryMainContentNav
            navigation={navigation.filter((n) => n.isActive)}
          />
        </SecondaryMainContentMenuArea>

        {/* Primary content */}
        <PrimaryContentArea>
          <Outlet />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
