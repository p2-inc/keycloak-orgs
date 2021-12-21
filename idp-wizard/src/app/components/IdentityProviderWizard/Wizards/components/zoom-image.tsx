import { Modal, ModalVariant } from "@patternfly/react-core";
import React, { FC, useState } from "react";

interface Props {
  src: string;
  alt: string;
}
export const StepImage: FC<Props> = ({ src, alt = "Step Image" }) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <Modal
        aria-label="Image"
        variant={ModalVariant.large}
        isOpen={isOpen}
        onClose={() => setIsOpen(false)}
        onKeyPress={(e) => (e.key === "Enter" ? setIsOpen(false) : null)}
        showClose={false}
      >
        <img
          src={src}
          alt={`Expanded ${alt}`}
          onClick={() => setIsOpen(false)}
          className="step-image-large"
          tabIndex={1}
        />
      </Modal>
      <img
        src={src}
        alt={alt}
        onClick={() => setIsOpen(true)}
        className="step-image"
      />
    </>
  );
};
