import React, { useState } from "react";
import DesktopSidebarNav from "../navs/desktop-sidebar-nav";
import { IconType } from "../icons";
import { Building2, User } from "lucide-react";
import { config } from "config";
const { features: featureFlags } = config.env;

export type NavigationItem = {
  name: string;
  href: string;
  icon: IconType;
  iconClass?: string;
  isActive: boolean;
};

const navigation: NavigationItem[] = [
  {
    name: "Profile",
    href: "/profile",
    icon: User,
    iconClass: "stroke-current",
    isActive: true,
  },
  {
    name: "Organizations",
    href: "/organizations",
    icon: Building2,
    iconClass: "fill-current",
    isActive: featureFlags.organizationsEnabled,
  },
];

export default function Layout({ children }: { children: React.ReactElement }) {
  const [menuCollapsed, setMenuCollapsed] = useState(true);

  return (
    <>
      <div className="flex h-screen dark:bg-p2dark-900">
        <div className="">
          {/* Static sidebar for desktop */}
          <DesktopSidebarNav
            navigation={navigation.filter((n) => n.isActive)}
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
