import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

export const Step1: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'On your Google Admin dashboard, select "Apps" from the sidebar menu, and then select "Web and Mobile Apps" from the following list.',
      component: <StepImage src={Images.GoogleSaml1A} alt="Step 1.1" />,
    },
    {
      text: 'On this page, select "Add App" and then "Add custom SAML app".',
      component: <StepImage src={Images.GoogleSaml1B} alt="Step 1.2" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Enterprise Application"
      instructionList={instructions}
    />
  );
};
