import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/okta/saml";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";
import { ClipboardCopyComponent } from "@wizardComponents";

interface Props {
  ssoUrl: string;
  audienceUri: string;
}

export const Step2: FC<Props> = ({ ssoUrl, audienceUri }) => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Single Sign-on URL"
          initialValue={ssoUrl}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Audience URI (SP Entity ID)"
          initialValue={audienceUri}
        />
      ),
    },
    {
      text: 'Submit the "Single sign on URL" and the "Audience URI".',
      component: (
        <img
          src={Images.OktaSaml4}
          alt="Step 2.3"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml4)}
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
        title="Step 2: Enter Service Provider Details"
        instructionList={instructions}
      />
    </>
  );
};
