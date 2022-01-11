import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

export const Step2: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: "Your identity provider should provide you with configuration information in the form of a configuration URL. This is sometimes called the OpenID Endpoint Configuration URL or the “Well-Known” Configuration",
      component: <></>,
    },
  ];

  return (
    <Step
      title="Step 2: Configure Application Configuration"
      instructionList={instructions}
    />
  );
};
