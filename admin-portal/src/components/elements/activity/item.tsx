import { FC } from "react";

type Props = {
  children: React.ReactNode;
  title: string;
};

const ActivityItem: FC<Props> = ({ children, title }) => {
  return (
    <div className="space-y-1">
      <div className="text-xs font-medium leading-3 text-secondary-800 dark:text-zinc-200">
        {children}
      </div>
      <div className="text-xs leading-3 text-secondary-800/50 dark:text-zinc-500">
        {title}
      </div>
    </div>
  );
};

export default ActivityItem;
