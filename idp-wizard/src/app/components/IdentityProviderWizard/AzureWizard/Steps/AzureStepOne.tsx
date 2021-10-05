import React, { useState } from "react";
import azureStep1Image from "@app/images/azure/azure-1.png";
import azureStep2Image from "@app/images/azure/azure-2.png";
import azureStep3Image from "@app/images/azure/azure-3.png";
import azureStep4Image from "@app/images/azure/azure-4.png";
import {
  Modal,
  ModalVariant,
  Stack,
  StackItem,
  Text,
  TextVariants,
  Title,
} from "@patternfly/react-core";

export function AzureStepOne() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalImageSrc, setModalImageSrc] = useState("");
  const onImageClick = (imageSrc) => {
    setModalImageSrc(imageSrc);
    setIsModalOpen(true);
  };

  return (
    <>
      <Modal
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Stack hasGutter>
        <StackItem>
          <Title headingLevel="h1">Step 1: Create Enterprise Application</Title>
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            Select "Enterprise applications" from your Azure AD dashboard.
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep1Image}
            alt="Step 1.1"
            className="step-image"
            onClick={() => onImageClick(azureStep1Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            Click "New application" and continue.
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep2Image}
            alt="Step 1.2"
            className="step-image"
            onClick={() => onImageClick(azureStep2Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            Select "Create your own application", then enter an App name that
            describers demo.phasetwo.io. Under "What are you looking to dow ithy
            our application?", select "Integrate any other application you don't
            find in the gallery (Non-gallery)", then select "Create".
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep3Image}
            alt="Step 1.3"
            className="step-image"
            onClick={() => onImageClick(azureStep3Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            Next, select "Single Sign On" from the "Manage" section in the left
            sidebar navigation menu, and then "SAML".
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep4Image}
            alt="Step 1.4"
            className="step-image"
            onClick={() => onImageClick(azureStep4Image)}
          />
        </StackItem>
      </Stack>
    </>
  );
}
