import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/lastpass";

interface Props {
  acsUrl: string;
  entityId: string;
}

export const LastPassStepThree: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Service Provider entity ID"
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
      text: "Paste the \"Service Provider entity ID\" and the \"Assertion Consumer Service URL\" into the appropriate fields.",
      component: <StepImage src={Images.LastPassSaml4} alt="Step 3.1" />,
    },
  ];

  return (
    <Step
      title="Step 3: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
