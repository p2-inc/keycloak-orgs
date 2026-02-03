import { FC } from "react";
import { IconProps } from ".";

export const ChevronIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    width="7"
    height="10"
    viewBox="0 0 7 10"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M1.77795 1.19159L5.27795 4.69159L1.77795 8.19159"
      strokeWidth="2"
      strokeLinecap="round"
    />
  </svg>
);
