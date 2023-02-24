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
    <div className={cs("block h-full")}>
      <div className="relative h-full">
        <div className="relative z-20 h-full">
          <div
            className={`col-span-1 flex h-full flex-col justify-between 
                      space-y-6 rounded-md border border-gray-200
                      py-9 px-10`}
          >
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrganizationActionCard;
