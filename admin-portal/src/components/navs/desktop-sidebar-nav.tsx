import { Popover } from "@headlessui/react";
import cs from "classnames";
import Button from "components/elements/forms/buttons/button";
import { config } from "config";
import useUser from "components/utils/useUser";
import { keycloak, keycloakService } from "keycloak";
import { ExternalLink } from "lucide-react";
import { NavLink, Link } from "react-router-dom";
import { ChevronIcon, DoubleSlashBrandIcon, FullBrandIcon } from "../icons";
import { NavigationItem } from "../layouts/layout";

import { useTranslation } from "react-i18next";
import ThemePicker from "./components/theme-picker";
import { useTheme } from "components/utils/useTheme";

type Props = {
  menuCollapsed: boolean;
  setMenuCollapsed: (collapsed: boolean) => void;
  navigation: NavigationItem[];
};

const DesktopSidebarNav: React.FC<Props> = ({
  menuCollapsed,
  setMenuCollapsed,
  navigation,
}) => {
  const { user, fullName } = useUser();
  const { theme, changeTheme } = useTheme();

  const { t } = useTranslation();
  const { appiconUrl, logoUrl } = config.env;

  return (
    <>
      <div className="flex h-full flex-shrink-0">
        <div
          className={cs(
            "flex w-[70px] flex-col border-r border-r-gray-100 transition-[width] duration-150 ease-in-out dark:border-r-zinc-800",
            {
              "w-64": !menuCollapsed,
            }
          )}
        >
          <div className="flex min-h-0 flex-1 flex-col bg-gray-50 dark:bg-p2dark-1000">
            <div className="flex-1">
              {menuCollapsed ? (
                <div
                  className="group flex items-center justify-center py-4 pt-8 hover:cursor-pointer "
                  onClick={() => setMenuCollapsed(!menuCollapsed)}
                >
                  {appiconUrl ? (
                    <img
                      src={appiconUrl}
                      className="h-full w-full max-w-[50px]"
                      alt="App Icon"
                    />
                  ) : (
                    <DoubleSlashBrandIcon />
                  )}
                  <ChevronIcon className="ml-[1px] stroke-gray-400 group-hover:stroke-gray-600" />
                </div>
              ) : (
                <div
                  className="group flex items-center justify-between py-4 pr-4 pl-8 pt-8 hover:cursor-pointer"
                  onClick={() => setMenuCollapsed(!menuCollapsed)}
                >
                  {logoUrl ? (
                    <img
                      src={logoUrl}
                      alt="App Icon"
                      className="w-auto max-w-[185px]"
                    />
                  ) : (
                    <FullBrandIcon />
                  )}
                  <ChevronIcon className="rotate-180 stroke-gray-400 group-hover:stroke-gray-600" />
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
                          "group flex items-center rounded-lg border-2 border-gray-200 p-[14px] text-sm transition-colors hover:border-gray-300 hover:bg-white dark:hover:border-zinc-600 dark:hover:bg-p2dark-900",
                          {
                            "dark:border-zinc-600 dark:text-white": !isActive,
                            "group:text-primary-700 border-primary-700 bg-white text-primary-700":
                              isActive,
                            "dark:border-zinc-400 dark:bg-p2dark-900 dark:text-white":
                              isActive,
                            "w-full border-0": !menuCollapsed,
                            "border-primary-700 text-primary-700 hover:border-primary-700 dark:hover:bg-p2dark-900":
                              menuCollapsed && isActive,
                          }
                        )
                      }
                    >
                      <item.icon className="h-5 w-5" />
                      <span className="sr-only">{t(item.name)}</span>
                      {!menuCollapsed && (
                        <span className="pl-2">{t(item.name)}</span>
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
                    <div className="mx-auto grid h-8 w-8 place-items-center rounded-full border border-primary-700 bg-white text-sm font-semibold dark:bg-zinc-400">
                      {fullName().substring(0, 1)}
                    </div>
                    {!menuCollapsed && (
                      <p className="ml-2 text-sm font-semibold dark:text-white">
                        {fullName()}
                      </p>
                    )}
                  </div>
                </Popover.Button>
                <Popover.Panel className="absolute bottom-10 left-0 z-[100] w-72 divide-y rounded-lg border bg-white px-5 shadow-lg dark:divide-zinc-700 dark:border-zinc-700 dark:bg-p2dark-900">
                  <div className="py-5">
                    <div className="font-semibold dark:text-gray-200">
                      {fullName()}
                    </div>
                    <div className="text-sm text-gray-500">{user?.email}</div>
                  </div>
                  <div className="py-1">
                    <Link
                      to="/"
                      className="group -mx-3 flex items-center justify-between rounded-md px-3 py-2 text-sm text-gray-700 transition hover:bg-gray-100 hover:text-gray-900 dark:text-gray-200 dark:hover:bg-zinc-800 dark:hover:text-gray-100"
                    >
                      <div>{t("returnToHomepage")}</div>
                      <ExternalLink className="h-4 w-4" />
                    </Link>
                  </div>
                  <div className="relative flex items-center justify-between py-2">
                    <ThemePicker theme={theme} changeTheme={changeTheme} />
                  </div>
                  <div className="py-5">
                    <a href={keycloak.createLogoutUrl()}>
                      <Button
                        className="w-full"
                        onClick={() => keycloakService.logout()}
                        title={t("logOut")}
                      >
                        {t("logOut")}
                      </Button>
                    </a>
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
