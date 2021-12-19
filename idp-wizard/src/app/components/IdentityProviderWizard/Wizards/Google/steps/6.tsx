import { useImageModal } from "@app/hooks/useImageModal";
import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/google";
import { Modal, ModalVariant } from "@patternfly/react-core";

interface Props {}

export const Step6: FC<Props> = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'In the created SAML app\'s landing page, select the "User Access Section".',
      component: (
        <img
          src={Images.GoogleSaml6A}
          alt="Step 6.1"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml6A)}
        />
      ),
    },
    {
      text: "Turn this service ON for the correct organizational units in your Google Directory setup. Save any changes. Note that Google may take up to 24 hours to propagate these changes, and the connection may be inactive until the changes have propagated.",
      component: (
        <img
          src={Images.GoogleSaml6B}
          alt="Step 6.2"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml6B)}
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
        title="Step 6: Configure User Access"
        instructionList={instructions}
      />
    </>
  );
};
