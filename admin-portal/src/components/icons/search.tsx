import { FC } from "react";
import { IconProps } from ".";

export const SearchIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    width="17"
    height="17"
    viewBox="0 0 17 17"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <rect
      x="1"
      y="1"
      width="11"
      height="11"
      rx="5.5"
      stroke="black"
      strokeWidth="2"
    />
    <path
      d="M11 11L15.5 15.5"
      stroke="black"
      strokeWidth="2"
      strokeLinecap="round"
    />
  </svg>
);
