import React, { FC } from "react";
import cs from "classnames";

export interface ButtonProps {
  text: string;
  image: string;
  active: boolean | false;
  onSelect?: () => void;
}

export const IdPButton: FC<ButtonProps> = ({
  text,
  image,
  active,
  onSelect,
}) => (
  <div
    className={cs("idp-button", {
      disabled: !active,
    })}
    onClick={onSelect}
  >
    <img src={image} alt={text} />
  </div>
);
