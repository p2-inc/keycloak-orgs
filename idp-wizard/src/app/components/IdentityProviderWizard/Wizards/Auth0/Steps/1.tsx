import React, { useState } from "react";
import Auth0Step1Image from "@app/images/auth0/auth0-1.png";
import Auth0Step2Image from "@app/images/auth0/auth0-2.png";
import { Modal, ModalVariant } from "@patternfly/react-core";
import { InstructionProps, Step } from "@wizardComponents";
import { useImageModal } from "@app/hooks/useImageModal";

export function Auth0StepOne() {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'In the Auth0 administrator portal, create a new application.',
      component: (
        <img
          src={Auth0Step1Image} 
          alt="Step 1.1"
          className="step-image"
          onClick={() => onImageClick(Auth0Step1Image)}
        />
      ),
    },
    {
      text: 'Name your application and select “Regular Web Applications” for application type.',
      component: (
        <img
          src={Auth0Step2Image}
          alt="Step 1.2"
          className="step-image"
          onClick={() => onImageClick(Auth0Step2Image)}
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
        title="Step 1: Create An Application "
        instructionList={instructions}
      />
    </>
  );
}
