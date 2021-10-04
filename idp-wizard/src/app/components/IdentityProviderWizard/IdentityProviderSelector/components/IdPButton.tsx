import React, { FC } from "react";

export interface ButtonProps {
  text: string;
  image: string;
  onSelect?: () => void;
}

export const IdPButton: FC<ButtonProps> = ({ text, image, onSelect }) => {
  return (
    <>
      <div className="idp-button" onClick={onSelect}>
        <img src={image} alt={text} />
      </div>
    </>
  );
};
