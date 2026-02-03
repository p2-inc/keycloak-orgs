import { FC } from "react";
import { IconProps } from ".";

export const ComputerIcon: FC<IconProps> = ({ className }) => (
  <svg
    width="17"
    height="17"
    viewBox="0 0 17 17"
    xmlns="http://www.w3.org/2000/svg"
    className={className}
  >
    <g clip-path="url(#clip0_238_3396)">
      <path d="M3.27832 0.5C1.61816 0.5 0.27832 1.83984 0.27832 3.5V10.5C0.27832 12.1602 1.61816 13.5 3.27832 13.5H13.2783C14.9385 13.5 16.2783 12.1602 16.2783 10.5V3.5C16.2783 1.83984 14.9385 0.5 13.2783 0.5H3.27832ZM3.27832 2.5H13.2783C13.833 2.5 14.2783 2.94531 14.2783 3.5V10.5C14.2783 11.0547 13.833 11.5 13.2783 11.5H3.27832C2.72363 11.5 2.27832 11.0547 2.27832 10.5V3.5C2.27832 2.94531 2.72363 2.5 3.27832 2.5ZM5.27832 14.5C4.17285 14.5 3.27832 15.3945 3.27832 16.5H13.2783C13.2783 15.3945 12.3838 14.5 11.2783 14.5H5.27832Z" />
    </g>
    <defs>
      <clipPath id="clip0_238_3396">
        <rect
          width="16"
          height="16"
          fill="white"
          transform="translate(0.27832 0.5)"
        />
      </clipPath>
    </defs>
  </svg>
);
