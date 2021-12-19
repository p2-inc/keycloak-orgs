import React, { FC } from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
} from "@wizardComponents";
import * as Images from "@app/images/okta/saml";
import { useImageModal } from "@app/hooks/useImageModal";
import { Modal, ModalVariant } from "@patternfly/react-core";

export const Step3: FC = () => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const instructions: InstructionProps[] = [
    {
      text: 'In the "Parameters" section, provide the following attribute mappings and select "Save". Note you may need to click "Add Another" to configure each of the mappings.',
      component: (
        <>
          <div className="pf-u-mb-md">
            Provide the following Attribute Mappings and select "Finish"
          </div>
          <DoubleItemClipboardCopy leftValue="email" rightValue="user.email" />
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="user.firstName"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="user.lastName"
          />
          <DoubleItemClipboardCopy leftValue="id" rightValue="user.login" />
          <div>
            Note that if "user.login" is not present in your Okta account, try
            "user.id" or do not add this mapping.
          </div>
        </>
      ),
    },
    {
      component: (
        <img
          src={Images.OktaSaml5}
          alt="Step 3.2"
          className="step-image"
          onClick={() => onImageClick(Images.OktaSaml5)}
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
        title="Step 3: Configure Attribute Mapping"
        instructionList={instructions}
      />
    </>
  );
};
