import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

export const Step4: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: "Text",
      component: <StepImage src={Images.GoogleSaml1A} alt="Step 1.1" />,
    },
  ];

  return (
    <Step
      title="Step 4: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
