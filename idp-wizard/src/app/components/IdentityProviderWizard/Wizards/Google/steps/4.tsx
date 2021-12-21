import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  ClipboardCopyComponent,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/google";

interface Props {
  acsUrl?: string;
  entityId?: string;
}

export const Step4: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this ACS URL"
          initialValue={acsUrl!}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Entity ID"
          initialValue={entityId!}
        />
      ),
    },
    {
      text: 'Submit the "ACS URL" and the "Entity ID". Then, click "Continue".',
      component: <StepImage src={Images.GoogleSaml4} alt="Step 4.2" />,
    },
  ];

  return (
    <Step
      title="Step 4: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
