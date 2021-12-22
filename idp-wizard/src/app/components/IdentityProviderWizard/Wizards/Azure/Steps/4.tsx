import React from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/azure/saml";

interface IClaims {
  name: string;
  value: string;
}

export function AzureStepFour() {
  const claimNames: IClaims[] = [
    {
      name: "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
      value: "user.mail",
    },
    {
      name: "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
      value: "user.givenname",
    },
    {
      name: "http://schemas.microsoft.com/identity/claims/name",
      value: "user.userprincipalname",
    },
    {
      name: "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
      value: "user.surname",
    },
  ];

  const instructionList: InstructionProps[] = [
    {
      text: "Click the Edit icon in the top right of the second step.",
      component: <StepImage src={Images.AzureSaml7} alt="Step 2.1" />,
    },
    {
      text: "Copy the following Claim names and Values",
      component: claimNames.map(
        ({ name: leftValue, value: rightValue }, index) => (
          <DoubleItemClipboardCopy
            leftValue={leftValue}
            rightValue={rightValue}
            key={index}
          />
        )
      ),
    },
    {
      text: 'Fill in the following Attribute Statements and select "Next".',
      component: <StepImage src={Images.AzureSaml8} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 4: User Attributes & Claims"
      instructionList={instructionList}
    />
  );
}
