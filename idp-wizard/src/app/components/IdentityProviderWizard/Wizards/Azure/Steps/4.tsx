import { Modal, ModalVariant } from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import azureStep9Image from "@app/images/azure/azure-9.png";
import azureStep10Image from "@app/images/azure/azure-10.png";
import azureStep11Image from "@app/images/azure/azure-11.png";
import azureStep12Image from "@app/images/azure/azure-12.png";
import { InstructionProps, Step } from "@wizardComponents";
import { useImageModal } from "@app/hooks/useImageModal";

export function AzureStepFour() {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  useEffect(() => {
    document?.getElementById("step")?.scrollIntoView();
  });

  const instructionList: InstructionProps[] = [
    {
      text: 'In order for your users and gorups of users to be synced to demo.phasetwo.io you will need to assign them to your Azure AD SAML Application. Select "Users and groups" from the "Manage" section of the navigations menu.',
      component: (
        <img
          src={azureStep9Image}
          alt="Step 4.1"
          className="step-image"
          onClick={() => onImageClick(azureStep9Image)}
        />
      ),
    },
    {
      text: 'Select "Add user/group" from the top menu.',
      component: (
        <img
          src={azureStep10Image}
          alt="Step 4.2"
          className="step-image"
          onClick={() => onImageClick(azureStep10Image)}
        />
      ),
    },
    {
      text: 'Select "None selected" under the "Users and Groups". In the menu, select the users and groups of users that you want to add to the SAML application, and click "Select".',
      component: (
        <img
          src={azureStep11Image}
          alt="Step 4.3"
          className="step-image"
          onClick={() => onImageClick(azureStep11Image)}
        />
      ),
    },
    {
      text: 'Select "Assign" to add the selected users and groups of users to your SAML application.',
      component: (
        <img
          src={azureStep12Image}
          alt="Step 4.4"
          className="step-image"
          onClick={() => onImageClick(azureStep12Image)}
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
        title="Step 4: Assign People & Groups"
        instructionList={instructionList}
      />
    </>
  );
}
