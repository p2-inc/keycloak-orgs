import cs from "classnames";
import { FC } from "react";

type Props = {
  children: React.ReactNode;
};

export const OACTopRow: FC<Props> = ({ children }) => (
  <div className="space-y-3 md:flex md:space-y-0 md:space-x-10">{children}</div>
);

const OrganizationActionCard: FC<Props> = ({ children }) => {
  return (
    <div className={cs("block")}>
      <div className="relative">
        <div className="relative z-20">
          <div className="col-span-1 flex flex-col justify-between space-y-6 rounded-md border border-gray-200 p-4 md:py-9 md:px-10 dark:border-zinc-600">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrganizationActionCard;
