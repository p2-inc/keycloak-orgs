import React from "react";
import Auth0Step3Image from "@app/images/auth0/SAML/auth0-3SAML.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";


export function Auth0StepTwo() {
  const instructions: InstructionProps[] = [
    {
      text: 'After creating your application, select the “Addons” tab, and enable the addon labeled “SAML2 WEB APP”',
      component: <StepImage src={Auth0Step3Image} alt="Step 2.1" />,
    },
  ];

  return (
     <Step
      title="Step 2: Select SAML Addon"
      instructionList={instructions}
    />
  );
}
