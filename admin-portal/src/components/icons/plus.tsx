import { FC } from "react";
import { IconProps } from ".";

export const PlusIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    width="18"
    height="18"
    viewBox="0 0 18 18"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <rect x="3" y="8" width="12" height="2" rx="1" />
    <rect
      x="8"
      y="15"
      width="12"
      height="2"
      rx="1"
      transform="rotate(-90 8 15)"
    />
  </svg>
);
