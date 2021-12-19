import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/google";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";

export const Step1: FC = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: "Text",
      component: (
        <img
          src={Images.GoogleSaml1A}
          alt="Step 1.1"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml1A)}
        />
      ),
    },
  ];

  return (
    <>
      <Modal
        aria-label="Image"
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Step
        title="Step 1: Create Enterprise Application"
        instructionList={instructions}
      />
    </>
  );
};
