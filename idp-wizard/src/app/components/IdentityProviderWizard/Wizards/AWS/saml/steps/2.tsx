import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/aws";

export const Step2: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'Enter a "Display Name" and an optional "Description". Click the "Copy URL" link next to the "AWS SSO SAML metadata file" URL, and paste it into the field below. This will load the AWS SSO configuration.',
      component: <StepImage src={Images.AWS_SSO_SAML_3} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 2: Upload AWS SSO IdP Information"
      instructionList={instructions}
    />
  );
};
