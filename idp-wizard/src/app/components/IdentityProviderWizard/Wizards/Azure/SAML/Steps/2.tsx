import React from "react";
import * as Images from "@app/images/azure/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { ClipboardCopyComponent } from "@wizardComponents";

export function AzureStepTwo() {
  const replyURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${process.env.AZURE_CUSTOMER_IDENTIFIER}/endpoint`;
  const identifierURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}`;

  const instructionList: InstructionProps[] = [
    {
      text: "Click the Edit icon in the top right of the first step.",
      component: <StepImage src={Images.AzureSaml5} alt="Step 2.1" />,
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this identifier"
          initialValue={identifierURL}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Reply URL"
          initialValue={replyURL}
        />
      ),
    },
    {
      text: "Submit the identifier and the Reply URL in the Basic SAML Configuration.",
      component: <StepImage src={Images.AzureSaml6} alt="Step 2.2" />,
    },
  ];

  return (
    <Step
      title="Step 2: Basic SAML Configuration"
      instructionList={instructionList}
    />
  );
}
