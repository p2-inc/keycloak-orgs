import React from "react";
import Auth0Step1Image from "@app/images/auth0/COMMON/auth0-1.png";
import Auth0Step2Image from "@app/images/auth0/COMMON/auth0-2.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";


export function Auth0StepOne() {
  const instructions: InstructionProps[] = [
    {
      text: 'In the Auth0 administrator portal, create a new application.',
      component: <StepImage src={Auth0Step1Image} alt="Step 1.1" />,
    },
    {
      text: 'Name your application and select “Regular Web Applications” for application type.',
      component: <StepImage src={Auth0Step2Image} alt="Step 1.2" />,
    },
  ];

  return (
     <Step
      title="Step 1: Create An Application "
      instructionList={instructions}
    />
  );
}
