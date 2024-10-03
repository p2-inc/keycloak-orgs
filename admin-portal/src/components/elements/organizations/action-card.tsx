import { FC } from "react";

type Props = {
  children: React.ReactNode;
};

export const OACTopRow: FC<Props> = ({ children }) => (
  <div className="flex-wrap gap-4 space-y-3 md:flex md:space-y-1 md:space-x-1">
    {children}
  </div>
);

const OrganizationActionCard: FC<Props> = ({ children }) => {
  return (
    <div className="block">
      <div className="relative h-full">
        <div className="relative z-20 h-full overflow-hidden">
          <div className="col-span-1 flex h-full flex-col justify-between space-y-6 rounded-md border border-gray-200 p-4 dark:border-zinc-600 md:py-9 md:px-10">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
};

export default OrganizationActionCard;
