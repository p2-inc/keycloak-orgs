import { FC } from "react";
import { IconProps } from ".";

export const XmarkIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    width="18"
    height="16"
    viewBox="0 0 18 16"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M2.25 14.75L15.75 1.25"
      stroke="black"
      strokeWidth="2"
      strokeLinecap="round"
      stroke-linejoin="round"
    />
    <path
      d="M15.75 14.75L2.25 1.25"
      stroke="black"
      strokeWidth="2"
      strokeLinecap="round"
      stroke-linejoin="round"
    />
  </svg>
);
