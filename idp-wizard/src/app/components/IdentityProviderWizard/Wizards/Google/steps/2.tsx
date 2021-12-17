import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/google";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";

export const Step2: FC = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'Enter an App name and icon (if applicable) for your application, then select "Continue".',
      component: (
        <img
          src={Images.GoogleSaml2}
          alt="Step 2.1"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml2)}
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
        title="Step 2: Enter Details for your Custom App"
        instructionList={instructions}
      />
    </>
  );
};
