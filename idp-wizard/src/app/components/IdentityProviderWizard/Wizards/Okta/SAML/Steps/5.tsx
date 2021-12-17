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

export const Step5: FC<Props> = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'In the "Assignments" section, choose people and groups that will have access to the application. In the "Assign" menu, you can select the type to add.',
      component: (
        <img
          src={Images.OktaSaml7}
          alt="Step 5.1"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml7)}
        />
      ),
    },
    {
      text: "In the subsequent pop up, you can select people or groups to assign to the application.",
      component: (
        <img
          src={Images.OktaSaml7A}
          alt="Step 5.2"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml7A)}
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
