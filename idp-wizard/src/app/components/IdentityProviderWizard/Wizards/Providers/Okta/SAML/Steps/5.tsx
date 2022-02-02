import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/okta/saml";

interface Props {}

export const Step5: FC<Props> = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In the "Assignments" section, choose people and groups that will have access to the application. In the "Assign" menu, you can select the type to add.',
      component: <StepImage src={Images.OktaSaml7} alt="Step 5.1" />,
    },
    {
      text: "In the subsequent pop up, you can select people or groups to assign to the application.",
      component: <StepImage src={Images.OktaSaml7A} alt="Step 5.2" />,
    },
  ];

  return (
    <Step
      title="Step 4: Complete Feedback Section"
      instructionList={instructions}
    />
  );
};
