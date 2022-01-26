import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/onelogin";

export const Step1: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In your OneLogin Administration dashboard, select "Applications" from the menu. On this page, select the "Add App" button.',
      component: <StepImage src={Images.OneLogin_SAML_1} alt="Step 1.1" />,
    },
    {
      text: "Use the search box to find “SAML Custom Connector (Advanced)” and select it.",
      component: <StepImage src={Images.OneLogin_SAML_2} alt="Step 1.2" />,
    },
    {
      text: "Enter an “Display Name” and “Description” and click “Save”.",
      component: <StepImage src={Images.OneLogin_SAML_3} alt="Step 1.3" />,
    },
  ];

  return (
    <Step
      title="Step 1: Add a SAML Application"
      instructionList={instructions}
    />
  );
};
