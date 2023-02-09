import { NavLink } from "react-router-dom";
import cs from "classnames";

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
  return (
    <nav className="space-y-1">
      {navigation.map((item) => (
        <NavLink
          key={item.name}
          to={item.href}
          className={({ isActive }) =>
            cs(
              {
                "font-semibold text-p2blue-700  hover:text-p2blue-700":
                  isActive,
                "text-gray-900 hover:bg-gray-50 hover:text-gray-900": !isActive,
              },
              "group flex items-center rounded-md px-3 py-2 text-sm "
            )
          }
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
  );
};

export default SecondaryMainContentNav;
