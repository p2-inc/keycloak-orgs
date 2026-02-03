import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

export const Step2: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'Enter an App name and icon (if applicable) for your application, then select "Continue".',
      component: <StepImage src={Images.GoogleSaml2} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 2: Enter Details for your Custom App"
      instructionList={instructions}
    />
  );
};
