import { NavLink, useLocation } from "react-router-dom";
import cs from "classnames";
import { useState } from "react";
import { ChevronIcon } from "components/icons";

type NavigationItem = {
  name: string;
  href: string;
  icon: React.ForwardRefExoticComponent<
    React.SVGProps<SVGSVGElement> & {
      title?: string | undefined;
      titleId?: string | undefined;
    }
  >;
};

type Props = {
  navigation: NavigationItem[];
};

const SecondaryMainContentNav: React.FC<Props> = ({ navigation }) => {
  const [isOpen, setOpen] = useState(false);
  const location = useLocation();
  const activeItem = navigation.find(
    (f) => f.href === location.pathname
  ) as NavigationItem;

  function toggleMenu() {
    setOpen(!isOpen);
  }

  return (
    <nav className="mb-4">
      <div className="md:hidden">
        {activeItem && (
          <button
            className="flex w-full items-center rounded-md border-2 border-gray-200 px-4 py-2 text-sm font-medium"
            onClick={toggleMenu}
          >
            <div className="flex items-center flex-1">
              <activeItem.icon
                className={cs("-ml-1 mr-3 h-6 w-6 flex-shrink-0")}
                aria-hidden="true"
              />
              {activeItem?.name}
            </div>
            <div className="flex-shrink-0">
              <ChevronIcon className="stroke-gray-600 rotate-90" />
            </div>
          </button>
        )}
      </div>
      <div className={cs("space-y-1 p-2", { "hidden md:block": !isOpen })}>
        {navigation.map((item) => (
          <NavLink
            key={item.name}
            to={item.href}
            className={({ isActive }) =>
              cs(
                {
                  "font-semibold text-p2blue-700  hover:text-p2blue-700":
                    isActive,
                  "text-gray-900 hover:bg-gray-50 hover:text-gray-900":
                    !isActive,
                },
                "group flex items-center rounded-md px-3 py-2 text-sm "
              )
            }
            onClick={toggleMenu}
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
      </div>
    </nav>
  );
};

export default SecondaryMainContentNav;
