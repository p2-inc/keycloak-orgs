import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/onelogin";

type Props = {
  acsUrl: string;
  recipient: string;
  acsUrlValidator: string;
  entityId: string;
};

export const Step2: FC<Props> = ({ acsUrl, recipient, acsUrlValidator, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'Navigate to "Configuration"',
      component: <StepImage src={Images.OneLogin_SAML_4} alt="Step 2.1" />,
    },
    {
      text: 'Submit the "Audience (Entity ID)", "Recipient", "ACS URL Validator" and the "ACS URL". Click “Save”.',
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this Audience (EntityID)"
            initialValue={entityId}
          />
          <ClipboardCopyComponent
            label="Copy this Recipient"
            initialValue={recipient}
          />
          <ClipboardCopyComponent
            label="Copy this ACS URL Validator"
            initialValue={acsUrlValidator}
          />
          <ClipboardCopyComponent
            label="Copy this ACS URL"
            initialValue={acsUrl}
          />
        </>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
