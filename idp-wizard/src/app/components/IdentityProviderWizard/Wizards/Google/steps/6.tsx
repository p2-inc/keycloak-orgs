import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

interface Props {}

export const Step6: FC<Props> = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In the created SAML app\'s landing page, select the "User Access Section".',
      component: <StepImage src={Images.GoogleSaml6A} alt="Step 6.1" />,
    },
    {
      text: "Turn this service ON for the correct organizational units in your Google Directory setup. Save any changes. Note that Google may take up to 24 hours to propagate these changes, and the connection may be inactive until the changes have propagated.",
      component: <StepImage src={Images.GoogleSaml6B} alt="Step 6.2" />,
    },
  ];

  return (
    <Step
      title="Step 6: Configure User Access"
      instructionList={instructions}
    />
  );
};
