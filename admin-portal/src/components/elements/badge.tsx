import { FC } from "react";
import cs from "classnames";

type Props = {
  children: React.ReactNode;
  className?: string;
};

const Badge: FC<Props> = ({ children, className }) => {
  return (
    <span
      className={cs(
        "inline-flex flex-shrink-0 items-center rounded-full border-2 border-gray-800 bg-white px-2.5 py-0.5 text-xs font-medium text-gray-800",
        className
      )}
    >
      {children}
    </span>
  );
};

export default Badge;
