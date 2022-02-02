import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  DoubleItemClipboardCopy,
} from "@wizardComponents";
import * as Images from "@app/images/jumpcloud";

// Mapping (click to copy) box
// UUID -> id
// Email Address -> email
// Given name -> firstName
// Family name -> lastName

export const Step3: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'Scroll down to find the "Attributes" section.',
      component: <StepImage src={Images.JumpCloud_SAML_6} alt="Step 3.1" />,
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="firstname"
            leftLabel="Name"
            rightLabel="JumpCloud Attribute Name"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="lastname"
            leftLabel="Name"
            rightLabel="JumpCloud Attribute Name"
          />
          <DoubleItemClipboardCopy
            leftValue="email"
            rightValue="email"
            leftLabel="Name"
            rightLabel="JumpCloud Attribute Name"
          />
        </>
      ),
    },
    {
      text: 'Provide the following attribute mappings by selecting "add attribute" in the "USER ATTRIBUTE MAPPING" section.',
      component: <StepImage src={Images.JumpCloud_SAML_7} alt="Step 3.2" />,
    },
  ];

  return (
    <Step
      title="Step 3: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
