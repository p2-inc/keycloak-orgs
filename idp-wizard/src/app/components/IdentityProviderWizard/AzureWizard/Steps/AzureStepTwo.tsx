import {
  Card,
  CardBody,
  ClipboardCopy,
  Form,
  FormGroup,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import azureStep5Image from "@app/images/azure/azure-5.png";
import azureStep6Image from "@app/images/azure/azure-6.png";
import { InstructionProps } from "../../InstructionComponent";
import Step from "../../Step";
import { useImageModal } from "@app/hooks/useImageModal";
import { ClipboardCopyComponent } from "../../ClipboardCopyComponent";

export function AzureStepTwo() {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  useEffect(() => {
    document?.getElementById("step")?.scrollIntoView();
  });

  const instructionList: InstructionProps[] = [
    {
      text: "Click the Edit icon in the top right of the first step.",
      component: (
        <img
          src={azureStep5Image}
          alt="Step 2.1"
          className="step-image"
          onClick={() => onImageClick(azureStep5Image)}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this identifier"
          initialValue="https://auth.phasetwo.io/30945803490g90rg493040"
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Reply URL"
          initialValue="https://auth.phasetwo.io/sso/saml/acs/30945803490g90rg493040"
        />
      ),
    },
    {
      text: "Submit the identifier and the Reply URL in the Basic SAML Configuration.",
      component: (
        <img
          src={azureStep6Image}
          alt="Step 2.2"
          className="step-image"
          onClick={() => onImageClick(azureStep6Image)}
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
        title="Step 2: Basic SAML Configuration"
        instructionList={instructionList}
      />
    </>
  );
}
