import React, { FC, useEffect, useState } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/google";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";

export const Step1 = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'On your Google Admin dashboard, select "Apps" from the sidebar menu, and then select "Web and Mobile Apps" from the following list.',
      component: (
        <img
          src={Images.GoogleSaml1A}
          alt="Step 1.1"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml1A)}
        />
      ),
    },
    {
      text: 'On this page, select "Add App" and then "Add custom SAML app".',
      component: (
        <img
          src={Images.GoogleSaml1B}
          alt="Step 1.2"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml1B)}
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
