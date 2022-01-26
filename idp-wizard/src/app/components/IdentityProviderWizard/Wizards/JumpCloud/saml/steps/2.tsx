import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/jumpcloud";

type Props = {
  acsUrl: string;
  entityId: string;
};

export const Step2: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>Select the "SSO" tab in your new application's configuration.</div>
      ),
    },
    {
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this IdP Entity ID"
            initialValue={entityId}
          />
          <ClipboardCopyComponent
            label="Copy this SP Entity ID"
            initialValue={entityId}
          />
          <ClipboardCopyComponent
            label="Copy this ACS URL"
            initialValue={acsUrl}
          />
        </>
      ),
    },
    {
      text: 'Paste the "IdP Entity ID", "SP Entity ID" and the "ACS URL" into the appropriate fields.',
      component: <StepImage src={Images.JumpCloud_SAML_4} alt="Step 2.1" />,
    },
    {
      text: 'Scroll down, and check the "Sign Assertion" checkbox. Leave all other fields as the provided defaults.',
      component: <StepImage src={Images.JumpCloud_SAML_5} alt="Step 2.2" />,
    },
  ];

  return (
    <Step
      title="Step 2: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
