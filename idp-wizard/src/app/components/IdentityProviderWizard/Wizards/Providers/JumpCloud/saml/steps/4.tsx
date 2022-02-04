import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/jumpcloud";

export const Step4: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In order for your users to be able to access the application, you must assign groups. Select the "User Groups" tab in your new application\'s configuration. Select groups you wish to have access to the application..',
      component: <StepImage src={Images.JumpCloud_SAML_8} alt="Step 4.1" />,
    },
    {
      text: 'Finally, select the "activate" button at the bottom of the screen.',
      component: <StepImage src={Images.JumpCloud_SAML_9} alt="Step 4.2" />,
    },
    {
      text: 'You may be prompted to confirm this selection. Select "continue".',
      component: <StepImage src={Images.JumpCloud_SAML_9a} alt="Step 4.3" />,
    },
  ];

  return <Step title="Step 4: Assign Groups" instructionList={instructions} />;
};
