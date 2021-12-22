import {
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import React from "react";
import Auth0Step4Image from "@app/images/auth0/auth0-4.png";
import { InstructionProps, Step, ClipboardCopyComponent } from "@wizardComponents";
import { useImageModal } from "@app/hooks/useImageModal";

export function Auth0StepThree() {

  const loginRedirectURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${process.env.AUTH0_CUSTOMER_IDENTIFIER}/endpoint`;
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

 

    const instructionList: InstructionProps[] = [
    {
      text: 'On the same page in your new app, scroll down to the “Application URIs” section, and paste the value below into the field named “Allowed Callback URLs”. Don’t forget to scroll down to the bottom and click “Save Changes”.',
      component: (
        <img
          src={Auth0Step4Image}
          alt="Step 3.1"
          className="step-image"
          onClick={() => onImageClick(Auth0Step4Image)}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this  login redirect URI"
          initialValue={loginRedirectURL}
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
        title="Step 3: Configure Redirect URI"
        instructionList={instructionList}
      />
    </>
  );
}
