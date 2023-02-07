import { FC } from "react";
import { IconProps } from ".";

export const ListIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    width="18"
    height="18"
    viewBox="0 0 18 18"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <rect x="2" y="4" width="14" height="2" rx="1" fill="black" />
    <rect x="2" y="8" width="14" height="2" rx="1" fill="black" />
    <rect x="2" y="12" width="14" height="2" rx="1" fill="black" />
  </svg>
);
