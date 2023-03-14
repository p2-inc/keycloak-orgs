import { FC } from "react";
import { getRoleSettings } from "services/role";

type Props = {
  name: string;
  className?: string;
};

const RoleBadge: FC<Props> = ({ name }) => {
  const settings = getRoleSettings(name);
  return (
    <span className="space-x-2 flex items-center px-1 py-1">
      <span className={`w-2.5 h-2.5 rounded-full inline-block flex-shrink-0 ${settings?.className}`}></span>
      <span className="inline-block text-sm">{name}</span>
    </span>
  );
};

export default RoleBadge;
