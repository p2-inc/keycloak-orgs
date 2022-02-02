import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

export const Step1: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: "Text",
      component: <StepImage src={Images.GoogleSaml1A} alt="Step 1.1" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Enterprise Application"
      instructionList={instructions}
    />
  );
};
