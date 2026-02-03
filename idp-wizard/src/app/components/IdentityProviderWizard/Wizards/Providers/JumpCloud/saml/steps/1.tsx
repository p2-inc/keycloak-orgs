import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/jumpcloud";

export const Step1: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In the JumpCloud console, select "SSO" from the menu. On this page, select the "+" button to add a new application.',
      component: <StepImage src={Images.JumpCloud_SAML_1} alt="Step 1.1" />,
    },
    {
      text: 'At the bottom of the page that opens, select "Custom SAML App".',
      component: <StepImage src={Images.JumpCloud_SAML_2} alt="Step 1.2" />,
    },
    {
      text: 'In the "General Info" tab enter a "Display Label" and an optional "Description".',
      component: <StepImage src={Images.JumpCloud_SAML_3} alt="Step 1.3" />,
    },
  ];

  return (
    <Step
      title="Step 1: Add a new SSO Application"
      instructionList={instructions}
    />
  );
};
