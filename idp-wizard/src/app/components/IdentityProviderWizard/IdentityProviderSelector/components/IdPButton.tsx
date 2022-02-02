import React, { FC } from "react";
import cs from "classnames";

export interface ButtonProps {
  text: string;
  image: string;
  active: boolean;
  noHover?: boolean;
  onSelect?: () => void;
}

export const IdPButton: FC<ButtonProps> = ({
  text,
  image,
  active,
  noHover,
  onSelect,
}) => (
  <div
    className={cs("idp-button", {
      disabled: !active,
      "no-hover": noHover,
    })}
    onClick={onSelect}
  >
    <img src={image} alt={text} />
  </div>
);
