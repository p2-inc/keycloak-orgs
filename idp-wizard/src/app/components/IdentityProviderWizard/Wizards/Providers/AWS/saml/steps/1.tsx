import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/aws";

export const Step1: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In the AWS SSO console, select "Applications" from the menu. On this page, select the "Add application" button.',
      component: <StepImage src={Images.AWS_SSO_SAML_1} alt="Step 1.1" />,
    },
    {
      text: 'The next screen is an application catalog. Select "Add custom SAML 2.0 application" at the top of the list, and then scroll down to the bottom and click "Next".',
      component: <StepImage src={Images.AWS_SSO_SAML_2} alt="Step 1.2" />,
    },
  ];

  return (
    <Step
      title="Step 1: Add a new SSO Application"
      instructionList={instructions}
    />
  );
};
