import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/pingone";

export const Step4: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In PingOne, group assignments are optional, as the default is to allow all groups to access each application. If you would like to restrict this application to a set of groups, select the "Edit" icon from the "Access" tab.',
      component: <StepImage src={Images.PINGONE_SAML_8} alt="Step 4.1" />,
    },
    {
      text: 'You can add groups assignments by clicking on the "+" icon next to each group name. Click "Save" when you have completed the assignments',
      component: <StepImage src={Images.PINGONE_SAML_9} alt="Step 4.2" />,
    },
  ];

  return (
    <Step
      title="Step 4: Assign Groups"
      instructionList={instructions}
    />
  );
};
