import React from "react";
import * as Images from "@app/images/lastpass";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function LastPassStepTwo() {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>Assign users and groups</b> section, enter a <b>Name</b> for the application,{" "}
          then select any Users or Groups that should have access to the application. 
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml3} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 2: Assign Users & Groups"
      instructionList={instructionList}
    />
  );
}
