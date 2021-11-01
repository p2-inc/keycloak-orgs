import React, { useState } from "react";
import azureStep1Image from "@app/images/azure/azure-1.png";
import azureStep2Image from "@app/images/azure/azure-2.png";
import azureStep3Image from "@app/images/azure/azure-3.png";
import azureStep4Image from "@app/images/azure/azure-4.png";
import {
  Flex,
  Modal,
  ModalVariant,
  Stack,
  StackItem,
  Text,
  TextVariants,
  Title,
} from "@patternfly/react-core";
import InstructionComponent, {
  InstructionProps,
} from "../../InstructionComponent";
import Step from "../../Step";

export function AzureStepOne() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalImageSrc, setModalImageSrc] = useState("");
  const onImageClick = (imageSrc) => {
    setModalImageSrc(imageSrc);
    setIsModalOpen(true);
  };

  const instructions: InstructionProps[] = [
    {
      text: 'Select "Enterprise applications" from your Azure AD dashboard.',
      component: (
        <img
          src={azureStep1Image}
          alt="Step 1.1"
          className="step-image"
          onClick={() => onImageClick(azureStep1Image)}
        />
      ),
    },
    {
      text: 'Click "New application" and continue.',
      component: (
        <img
          src={azureStep2Image}
          alt="Step 1.2"
          className="step-image"
          onClick={() => onImageClick(azureStep2Image)}
        />
      ),
    },
    {
      text: 'Select "Create your own application", then enter an App name that describers demo.phasetwo.io. Under "What are you looking to do with your application?", select "Integrate any other application you dont find in the gallery (Non-gallery)", then select "Create".',
      component: (
        <img
          src={azureStep3Image}
          alt="Step 1.3"
          className="step-image"
          onClick={() => onImageClick(azureStep3Image)}
        />
      ),
    },
    {
      text: 'Next, select "Single Sign On" from the "Manage" section in the left sidebar navigation menu, and then "SAML".',
      component: (
        <img
          src={azureStep4Image}
          alt="Step 1.4"
          className="step-image"
          onClick={() => onImageClick(azureStep4Image)}
        />
      ),
    },
  ];

  return (
    <>
      <Modal
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
}
