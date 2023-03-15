import { Menu, Popover } from "@headlessui/react";
import cs from "classnames";
import Button from "components/elements/forms/buttons/button";
import { ExternalLink } from "lucide-react";
import { NavLink, Link } from "react-router-dom";
import { ChevronIcon, DoubleSlashBrandIcon, FullBrandIcon } from "../icons";
import { NavigationItem, UserInfo } from "../layouts/layout";

type Props = {
  menuCollapsed: boolean;
  setMenuCollapsed: (collapsed: boolean) => void;
  navigation: NavigationItem[];
  user: UserInfo;
};

const DesktopSidebarNav: React.FC<Props> = ({
  menuCollapsed,
  setMenuCollapsed,
  navigation,
  user,
}) => {
  return (
    <>
      {/* If using a mobile view: <div className="hidden lg:flex lg:flex-shrink-0"> */}
      <div className="flex h-full flex-shrink-0">
        <div
          className={cs(
            "flex w-[70px] flex-col border-r border-r-gray-100 transition-[width] duration-150 ease-in-out",
            {
              "w-64": !menuCollapsed,
            }
          )}
        >
          <div className="flex min-h-0 flex-1 flex-col bg-gray-50">
            <div className="flex-1">
              {menuCollapsed ? (
                <div
                  className="group flex items-center justify-center py-4 pt-8 hover:cursor-pointer "
                  onClick={() => setMenuCollapsed(!menuCollapsed)}
                >
                  <DoubleSlashBrandIcon />
                  <ChevronIcon className="stroke-gray-400 group-hover:stroke-gray-600 " />
                </div>
              ) : (
                <div
                  className="group flex items-center justify-between py-4 pr-4 pl-8 pt-8 hover:cursor-pointer"
                  onClick={() => setMenuCollapsed(!menuCollapsed)}
                >
                  <FullBrandIcon />
                  <ChevronIcon className="rotate-180 stroke-gray-400 group-hover:stroke-gray-600 " />
                </div>
              )}
              <nav
                aria-label="Sidebar"
                className={cs(
                  "flex flex-col items-center space-y-3 py-6 font-semibold",
                  {
                    "px-4": !menuCollapsed,
                  }
                )}
              >
                {navigation.map((item) => {
                  return (
                    <NavLink
                      key={item.name}
                      to={item.href}
                      className={({ isActive }) =>
                        cs(
                          "group flex items-center rounded-lg border-2 border-gray-200 p-[14px] text-sm transition-colors hover:border-gray-300 hover:bg-white",
                          {
                            "group:text-p2blue-700 border-p2blue-700 bg-white text-p2blue-700":
                              isActive,
                            "w-full border-0": !menuCollapsed,
                            "border-p2blue-700 text-p2blue-700 hover:border-p2blue-700":
                              menuCollapsed && isActive,
                          }
                        )
                      }
                    >
                      <item.icon
                        className="h-5 w-5"
                        // className={cs("h-[18] w-[18]", item.iconClass)}
                        // aria-hidden="true"
                      />
                      <span className="sr-only">{item.name}</span>
                      {!menuCollapsed && (
                        <span className="pl-2">{item.name}</span>
                      )}
                    </NavLink>
                  );
                })}
              </nav>
            </div>
            <div
              className={cs("p-5", {
                "flex flex-shrink-0 justify-center": menuCollapsed,
                "": !menuCollapsed,
              })}
            >
              <Popover className="relative">
                <Popover.Button className="outline-none">
                  <div className="flex items-center">
                    <div className="mx-auto grid h-8 w-8 place-items-center rounded-full bg-white text-sm font-semibold">
                      {user.name.substring(0, 1)}
                    </div>
                    {!menuCollapsed && (
                      <p className="ml-2 text-sm font-semibold">{user.name}</p>
                    )}
                  </div>
                </Popover.Button>
                <Popover.Panel className="absolute bottom-10 left-0 z-[100] w-72 divide-y bg-white px-5 shadow-lg">
                  <div className="py-5">
                    <div className="font-semibold">{user.name}</div>
                    <div className="text-sm text-gray-500">{user.email}</div>
                  </div>
                  <div className="py-1">
                    <Link
                      to=""
                      className="group -mx-3 flex items-center rounded-md px-3 py-2 text-sm text-gray-700 transition hover:bg-gray-100 hover:text-gray-900 justify-between"
                    >
                      <div>Return to homepage</div>
                      <ExternalLink className="w-4 h-4" />
                    </Link>
                  </div>
                  <div className="py-5">
                    <Button className="w-full">Log Out</Button>
                  </div>
                </Popover.Panel>
              </Popover>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default DesktopSidebarNav;
