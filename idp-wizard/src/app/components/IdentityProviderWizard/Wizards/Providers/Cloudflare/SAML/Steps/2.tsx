import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/cloudflare/saml";

interface Props {
  acsUrl: string;
  entityId: string;
}

export const CloudflareStepTwo: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Entity ID"
          initialValue={entityId}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Assertion Consumer Service URL"
          initialValue={acsUrl}
        />
      ),
    },
    {
      text: "Paste the \"Entity ID\" and the \"Assertion Consumer Service URL\" into the appropriate fields.",
      component: <StepImage src={Images.CloudflareSaml3} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 2: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
