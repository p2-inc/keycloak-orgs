import React, { useState } from "react";
import DesktopSidebarNav from "../navs/desktop-sidebar-nav";
import { IconType, PeopleIcon, PersonIcon } from "../icons";

export type User = {
  name: string;
  email: string;
};

const user = {
  name: "Garth Patil",
  email: "garth@phasetwo.io",
};

export type NavigationItem = {
  name: string;
  href: string;
  icon: IconType;
  iconClass?: string;
};

const navigation: NavigationItem[] = [
  {
    name: "Profile",
    href: "/profile",
    icon: PersonIcon,
    iconClass: "stroke-current",
  },
  {
    name: "Organizations",
    href: "/organizations",
    icon: PeopleIcon,
    iconClass: "fill-current",
  },
];

export default function Layout({ children }: { children: React.ReactElement }) {
  const [menuCollapsed, setMenuCollapsed] = useState(true);

  return (
    <>
      <div className="flex h-full antialiased">
        {/* Static sidebar for desktop */}
        <DesktopSidebarNav
          navigation={navigation}
          user={user}
          setMenuCollapsed={setMenuCollapsed}
          menuCollapsed={menuCollapsed}
        />

        <div className="flex min-w-0 flex-1 flex-col overflow-hidden">
          <main className="flex flex-1 flex-col overflow-hidden">
            {children}
          </main>
        </div>
      </div>
    </>
  );
}
