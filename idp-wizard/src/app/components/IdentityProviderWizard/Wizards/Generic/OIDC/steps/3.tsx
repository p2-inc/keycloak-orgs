import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/google";

export const Step3: FC = () => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          Your identity provider should provide you with credentials that will
          allow you to authenticate with demo.phasetwo.io. These are usually
          called a Client ID and a Client Secret or Key.
        </div>
      ),
    },
    {
      component: <div>Form</div>,
    },
  ];

  return (
    <Step
      title="Step 3: Provide The Client Credentials"
      instructionList={instructions}
    />
  );
};
