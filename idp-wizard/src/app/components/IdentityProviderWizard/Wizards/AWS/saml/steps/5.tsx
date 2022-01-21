import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/aws";

export const Step5: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'In order for your users to be able to access the application, you must assign users and/or groups. Start by opening the "Assigned users" tab and selecting "Assign users".',
      component: <StepImage src={Images.AWS_SSO_SAML_6} alt="Step 5.1" />,
    },
    {
      text: "You can add individual users.",
      component: <StepImage src={Images.AWS_SSO_SAML_7} alt="Step 5.2" />,
    },
    {
      text: "You can also add groups of users. This is the preferred way to manage user access.",
      component: <StepImage src={Images.AWS_SSO_SAML_8} alt="Step 5.3" />,
    },
  ];

  return (
    <Step
      title="Step 5: Assign Users and Groups"
      instructionList={instructions}
    />
  );
};
