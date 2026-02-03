import { FC } from "react";
import { IconProps } from ".";

export const GridIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 18 18"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <rect
      x="1"
      y="1"
      width="6"
      height="6"
      rx="2"
      stroke="black"
      strokeWidth="2"
    />
    <rect
      x="1"
      y="11"
      width="6"
      height="6"
      rx="2"
      stroke="black"
      strokeWidth="2"
    />
    <rect
      x="11"
      y="1"
      width="6"
      height="6"
      rx="2"
      stroke="black"
      strokeWidth="2"
    />
    <rect
      x="11"
      y="11"
      width="6"
      height="6"
      rx="2"
      stroke="black"
      strokeWidth="2"
    />
  </svg>
);
