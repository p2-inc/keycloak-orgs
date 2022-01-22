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
  entityId: string;
};

export const Step2: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'Navigate to "Configuration"',
      component: <StepImage src={Images.OneLogin_SAML_4} alt="Step 2.1" />,
    },
    {
      text: 'Submit the "Entity ID", "ACS URL Validator" and the "ACS URL". Click “Save”.',
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this Entity ID"
            initialValue={entityId}
          />
          <ClipboardCopyComponent
            label="Copy this ACS URL Validator"
            initialValue={acsUrl}
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
