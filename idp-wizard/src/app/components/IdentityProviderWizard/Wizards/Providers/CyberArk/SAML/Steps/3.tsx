import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/cyberark/SAML";

interface Props {
  acsUrl: string;
  entityId: string;
}

export const CyberArkStepThree: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Scroll down to the <b>Service Provider Configuration</b> section of the{" "}
          <b>Trust</b> page, set the configuration method to <b>Manual Configuration</b>{" "}
          then paste the <b>SP Entity ID</b> and <b>Assertion Consumer Service (ACS) URL</b>{" "}
          from below into the appropriate fields.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml4} alt="Step 3.1" />,
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this SP Entity ID"
          initialValue={entityId}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Assertion Consumer Service (ACS) URL"
          initialValue={acsUrl}
        />
      ),
    },
    {
      component: (
        <div>
          Click the <b>Save</b> button at the bottom of the page to save your changes.
        </div>
      )
    },
    
  ];

  return (
    <Step
      title="Step 3: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
