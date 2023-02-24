import cs from "classnames";
import { FC } from "react";

type Props = {
  children: React.ReactNode;
};

export const OACTopRow: FC<Props> = ({ children }) => (
  <div className="flex space-x-10">{children}</div>
);

const OrganizationActionCard: FC<Props> = ({ children }) => {
  return (
    <div
      className={cs(
        "group block h-full",
        "focus:outline-none focus:ring-1 focus:ring-neutral-50 focus:ring-offset-1"
      )}
    >
      <div className="relative h-full">
        <div className="relative z-20 h-full">
          <div
            className={`col-span-1 flex h-full flex-col justify-between 
                      space-y-6 rounded-md border border-gray-200 bg-slate-50 
                      py-9 px-10 group-hover:border-gray-300 group-hover:bg-white`}
          >
            {children}
          </div>
        </div>
        <div
          className={cs(
            "absolute inset-x-3 bottom-0 z-10 h-1/2 rounded-full bg-white opacity-0",
            "transition-opacity duration-200",
            "group-hover:opacity-100",
            "drop-shadow-btn-light group-active:hidden"
          )}
        />
      </div>
    </div>
  );
};

export default OrganizationActionCard;
