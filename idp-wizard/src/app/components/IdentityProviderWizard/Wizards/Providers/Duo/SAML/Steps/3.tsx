import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/duo/saml";

interface Props {
  acsUrl: string;
  entityId: string;
}

export const DuoStepThree: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this identifier"
          initialValue={entityId}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Reply URL"
          initialValue={acsUrl}
        />
      ),
    },
    {
      text: "Submit the Entity ID and the ACS URL.",
      component: <></>,
    },
    {
      component: <StepImage src={Images.DuoSaml3} alt="Step 3.1" />,
    },
  ];

  return (
    <Step
      title="Step 3: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
