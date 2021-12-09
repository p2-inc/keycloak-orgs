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
import { InstructionProps } from "../../../InstructionComponent";
import { Step } from "../../components";
import { useImageModal } from "@app/hooks/useImageModal";
import { ClipboardCopyComponent } from "../../components/clipboard-copy";

export function AzureStepTwo() {
  const replyURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${process.env.AZURE_CUSTOMER_IDENTIFIER}/endpoint`;
  const identifierURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}`;
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
          initialValue={identifierURL}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Reply URL"
          initialValue={replyURL}
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
