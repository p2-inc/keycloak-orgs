import React, { FC } from "react";

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
}) => {
  let extraClass = "";
  if (!active) {
    extraClass = "disabled";
  }
  return (
    <div className={"idp-button " + extraClass} onClick={onSelect}>
      <img src={image} alt={text} />
    </div>
  );
};
