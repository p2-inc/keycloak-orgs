import {
  Card,
  CardBody,
  ClipboardCopy,
  Form,
  Title,
  Stack,
  StackItem,
  Text,
  TextVariants,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import azureStep9Image from "@app/images/azure/azure-9.png";
import azureStep10Image from "@app/images/azure/azure-10.png";
import azureStep11Image from "@app/images/azure/azure-11.png";
import azureStep12Image from "@app/images/azure/azure-12.png";

export function AzureStepFour() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalImageSrc, setModalImageSrc] = useState("");
  const onImageClick = (imageSrc) => {
    setModalImageSrc(imageSrc);
    setIsModalOpen(true);
  };

  useEffect(() => {
    document?.getElementById("step")?.scrollIntoView();
  });

  return (
    <>
      <Modal
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Stack id="step">
        <StackItem>
          <Title headingLevel="h1">Step 4: Assign People & Groups</Title>
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            <br />
            In order for your users and gorups of users to be synced to
            demo.phasetwo.io you will need to assign them to your Azure AD SAML
            Application. Select "Users and groups" from the "Manage" section of
            the navigations menu.
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep9Image}
            alt="Step 4.1"
            className="step-image"
            onClick={() => onImageClick(azureStep9Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            <br />
            Select "Add user/group" from the top menu.
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep10Image}
            alt="Step 4.2"
            className="step-image"
            onClick={() => onImageClick(azureStep10Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            <br />
            Select "None selected" under the "Users and Groups". In the menu,
            select the users and groups of users that you want to add to the
            SAML application, and click "Select".
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep11Image}
            alt="Step 4.3"
            className="step-image"
            onClick={() => onImageClick(azureStep11Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            <br />
            Select "Assign" to add the selected users and groups of users to
            your SAML application.
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep12Image}
            alt="Step 4.4"
            className="step-image"
            onClick={() => onImageClick(azureStep12Image)}
          />
        </StackItem>
      </Stack>
    </>
  );
}
