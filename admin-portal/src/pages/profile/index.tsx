import SecondaryMainContentMenu from "components/navs/secondary-main-content-menu";
import FormTextInputWithLabel from "components/elements/forms/inputs/text-input-with-label";
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
import PrimaryContentArea from "components/navs/primary-content";
import GeneralProfile from "./general";

const navigation = [
  {
    name: "General",
    href: "/profile/general",
    icon: UserCircleIcon,
    current: true,
  },
  {
    name: "Signing in",
    href: "/profile/signin",
    icon: KeyIcon,
    current: false,
  },
  {
    name: "Device activity",
    href: "/profile/activity",
    icon: DevicePhoneMobileIcon,
    current: false,
  },
  {
    name: "Linked accounts",
    href: "/profile/linked",
    icon: SquaresPlusIcon,
    current: false,
  },
];

export default function Profile() {
  return (
    <>
      <TopHeader header="Profile" />
      <FixedWidthMainContent>
        {/* Secondary menu */}
        <SecondaryMainContentMenu>
          <nav className="space-y-1">
            {navigation.map((item) => (
              <NavLink
                key={item.name}
                to={item.href}
                className={({ isActive }) =>
                  cs(
                    {
                      "bg-gray-50 text-p2blue-700 hover:bg-white hover:text-p2blue-700":
                        isActive,
                      "text-gray-900 hover:bg-gray-50 hover:text-gray-900":
                        !isActive,
                    },
                    "group flex items-center rounded-md px-3 py-2 text-sm font-medium"
                  )
                }
                aria-current={item.current ? "page" : undefined}
              >
                <>
                  <item.icon
                    className={cs("-ml-1 mr-3 h-6 w-6 flex-shrink-0")}
                    aria-hidden="true"
                  />
                  <span className="truncate">{item.name}</span>
                </>
              </NavLink>
            ))}
          </nav>
        </SecondaryMainContentMenu>

        {/* Primary content */}
        <PrimaryContentArea>
          <GeneralProfile />
        </PrimaryContentArea>
      </FixedWidthMainContent>
    </>
  );
}
