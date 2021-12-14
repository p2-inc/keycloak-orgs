import { useImageModal } from "@app/hooks/useImageModal";
import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  ClipboardCopyComponent,
} from "@wizardComponents";
import * as Images from "@app/images/google";
import { Modal, ModalVariant } from "@patternfly/react-core";

interface Props {
  acsUrl?: string;
  entityId?: string;
}

export const Step4: FC<Props> = ({ acsUrl, entityId }) => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this ACS URL"
          initialValue={acsUrl!}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Entity ID"
          initialValue={entityId!}
        />
      ),
    },
    {
      text: 'Submit the "ACS URL" and the "Entity ID". Then, click "Continue".',
      component: (
        <img
          src={Images.GoogleSaml4}
          alt="Step 4.2"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml4)}
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
        title="Step 4: Enter Service Provider Details"
        instructionList={instructions}
      />
    </>
  );
};
