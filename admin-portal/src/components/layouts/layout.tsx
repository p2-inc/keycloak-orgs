import React, { useState } from "react";
import DesktopSidebarNav from "../navs/desktop-sidebar-nav";
import { IconType, PeopleIcon, PersonIcon } from "../icons";
import { Building2, User, Users } from "lucide-react";
import { config } from "config";
const featureFlags = config.env.features;

export type NavigationItem = {
  name: string;
  href: string;
  icon: IconType;
  iconClass?: string;
};

const navigation: NavigationItem[] = [];

const addToNavigation = (data: NavigationItem, condition: boolean) => {
  if (condition) {
    navigation.push(data);
  }
};

addToNavigation(
  {
    name: "Profile",
    href: "/profile",
    icon: User,
    iconClass: "stroke-current",
  },
  true
);
addToNavigation(
  {
    name: "Organizations",
    href: "/organizations",
    icon: Building2,
    iconClass: "fill-current",
  },
  featureFlags.organizationsEnabled
);

export default function Layout({ children }: { children: React.ReactElement }) {
  const [menuCollapsed, setMenuCollapsed] = useState(true);

  return (
    <>
      <div className="flex h-screen">
        <div className="">
          {/* Static sidebar for desktop */}
          <DesktopSidebarNav
            navigation={navigation}
            setMenuCollapsed={setMenuCollapsed}
            menuCollapsed={menuCollapsed}
          />
        </div>
        <div className="flex-1 overflow-y-auto pb-20">
          <div className="">
            <main className="m-auto max-w-7xl">{children}</main>
          </div>
        </div>
      </div>
    </>
  );
}
