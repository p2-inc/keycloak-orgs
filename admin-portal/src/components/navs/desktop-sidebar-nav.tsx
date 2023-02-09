import cs from "classnames";
import { NavLink, Link } from "react-router-dom";
import { ChevronIcon, DoubleSlashBrandIcon, FullBrandIcon } from "../icons";
import { NavigationItem, User } from "../layouts/layout";

type Props = {
  menuCollapsed: boolean;
  setMenuCollapsed: (collapsed: boolean) => void;
  navigation: NavigationItem[];
  user: User;
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
      <div className="flex flex-shrink-0">
        <div
          className={cs(
            "flex w-[70px] flex-col transition-[width] duration-150 ease-in-out",
            {
              "w-64": !menuCollapsed,
            }
          )}
        >
          <div className="flex min-h-0 flex-1 flex-col overflow-y-auto bg-gray-50">
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
                        className={cs("h-[18] w-[18]", item.iconClass)}
                        aria-hidden="true"
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
              className={cs("flex pb-5", {
                "flex-shrink-0": menuCollapsed,
              })}
            >
              <Link
                to="#"
                className={cs("flex  font-semibold", {
                  "items-center px-6": !menuCollapsed,
                  "w-full flex-shrink-0": menuCollapsed,
                })}
              >
                <div className="mx-auto block grid h-8 w-8 place-items-center rounded-full bg-white text-sm">
                  {user.name.substring(0, 1)}
                </div>
                {!menuCollapsed && <p className="ml-2 text-sm">{user.name}</p>}
                <div className="sr-only">
                  <p>{user.name}</p>
                  <p>Account settings</p>
                </div>
              </Link>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default DesktopSidebarNav;
