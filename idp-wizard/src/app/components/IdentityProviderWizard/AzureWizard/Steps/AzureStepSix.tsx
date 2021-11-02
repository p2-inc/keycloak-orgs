import {
  Card,
  CardBody,
  Title,
  Form,
  FormGroup,
  Stack,
  StackItem,
  Text,
  TextInput,
  TextVariants,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import azureStep14Image from "@app/images/azure/azure-14.png";
import { InstructionProps } from "../../InstructionComponent";
import Step from "../../Step";
import { useImageModal } from "@app/hooks/useImageModal";

export function AzureStepSix() {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  useEffect(() => {
    document?.getElementById("step")?.scrollIntoView();
  });

  const instructionList: InstructionProps[] = [
    {
      text: "Copy the Login URL from Step 4 and enter it below.",
      component: (
        <img
          src={azureStep14Image}
          alt="Step 6.1"
          className="step-image"
          onClick={() => onImageClick(azureStep14Image)}
        />
      ),
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            <Form>
              <FormGroup label="Login URL" fieldId="copy-form">
                <TextInput
                  name="Login URL"
                  id="loginURL"
                  aria-label="Login URL"
                  value={"https://app.phasetwo.io/realms/test/saml"}
                />
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
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
        title="Step 6: Provide a Login URL"
        instructionList={instructionList}
      />
    </>
  );
}
