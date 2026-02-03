import { FC } from "react";
import { IconProps } from ".";

export const CheckmarkIcon: FC<IconProps> = ({ className }) => (
  <svg
    className={className}
    width="22"
    height="17"
    viewBox="0 0 22 17"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path
      d="M1.75 10.25L6.75 15.25L20.25 1.75"
      stroke="black"
      strokeWidth="2"
      strokeLinecap="round"
      stroke-linejoin="round"
    />
  </svg>
);
