import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/okta/saml";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";

export const Step1: FC = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'In your Okta Administration dashboard, select "Applications" from the menu. On this page, select the "Create App Integration" button.',
      component: (
        <img
          src={Images.OktaSaml1}
          alt="Step 1.1"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml1)}
        />
      ),
    },
    {
      text: 'Select "SAML 2.0" for the "Sign-in method" and click "Next"',
      component: (
        <img
          src={Images.OktaSaml2}
          alt="Step 1.2"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml2)}
        />
      ),
    },
    {
      text: 'Enter an "App name" and click "Next".',
      component: (
        <img
          src={Images.OktaSaml3}
          alt="Step 1.3"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml3)}
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
