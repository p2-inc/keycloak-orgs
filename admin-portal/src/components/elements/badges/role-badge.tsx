import { FC } from "react";
import { getRoleSettings } from "services/role";

type Props = {
  name: string;
  className?: string;
};

const RoleBadge: FC<Props> = ({ name }) => {
  const settings = getRoleSettings(name);
  return (
    <span className="flex items-center space-x-2 px-1 py-1">
      <span
        className={`inline-block h-2.5 w-2.5 flex-shrink-0 rounded-full ${settings?.className}`}
      ></span>
      <span className="inline-block text-sm dark:text-zinc-200">{name}</span>
    </span>
  );
};

export default RoleBadge;
