import React, { FC } from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
} from "@wizardComponents";
import * as Images from "@app/images/okta/saml";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";

interface Props {}

export const Step4: FC<Props> = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'Okta requires customer feedback. Select the option "I’m an Okta customer adding an internal app", click "Finish" and then leave the additional form blank.',
      component: (
        <img
          src={Images.OktaSaml6}
          alt="Step 4.1"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml6)}
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
        title="Step 4: Complete Feedback Section"
        instructionList={instructions}
      />
    </>
  );
};
