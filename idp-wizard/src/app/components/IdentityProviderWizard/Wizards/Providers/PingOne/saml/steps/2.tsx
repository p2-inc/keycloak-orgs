import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/pingone";

type Props = {
  acsUrl: string;
  entityId: string;
};

export const Step2: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this ACS URL"
            initialValue={acsUrl}
          />
          <ClipboardCopyComponent
            label="Copy this Entity ID"
            initialValue={entityId}
          />
        </>
      ),
    },
    {
      text: 'Submit the Assertion Consumer Service Endpoint as the "ACS URL" and the "Entity ID".',
      component: <StepImage src={Images.PINGONE_SAML_4} alt="Step 2.1" />,
    },
    {
      text: 'Scroll down and input "600" for "Assertion Validity Duration (in seconds)", then select "Save and Continue".',
      component: <StepImage src={Images.PINGONE_SAML_5} alt="Step 2.2" />,
    },
  ];

  return (
    <Step
      title="Step 2: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
