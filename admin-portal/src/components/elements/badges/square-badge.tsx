import { FC } from "react";
import cs from "classnames";

type Props = {
  children: React.ReactNode;
  className?: string;
};

const SquareBadge: FC<Props> = ({ children, className }) => {
  return (
    <span
      className={cs(
        "rounded border border-p2gray-900 px-1 py-px font-mono text-xs font-medium",
        className
      )}
    >
      {children}
    </span>
  );
};

export default SquareBadge;
