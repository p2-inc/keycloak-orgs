import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/onelogin";

export const Step4: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In OneLogin, access policies are optional, as the default is to allow all users to access each application. If you would like to restrict this application by policy, select the "Access" tab, and click "Save" when you have setup your selected access policies.',
      component: <StepImage src={Images.OneLogin_SAML_7} alt="Step 4.1" />,
    },
  ];

  return (
    <Step
      title="Step 4: Access Policy"
      instructionList={instructions}
    />
  );
};
