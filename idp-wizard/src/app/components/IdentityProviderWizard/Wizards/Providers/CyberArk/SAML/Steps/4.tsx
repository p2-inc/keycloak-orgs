import React from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/cyberark/SAML";

export function CyberArkStepFour() {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Switch to the <b>SAML Response</b> page, and provide the following
          attribute mappings in the <b>Attributes</b> section. Click <b>Add</b> for
          each attribute listed below.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml5} alt="Step 4.1" />,
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="LoginUser.FirstName"
            leftLabel="Attribute Name"
            rightLabel="Attribute Value"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="LoginUser.LastName"
            leftLabel="Attribute Name"
            rightLabel="Attribute Value"
          />
          <DoubleItemClipboardCopy
            leftValue="username"
            rightValue="LoginUser.Username"
            leftLabel="Attribute Name"
            rightLabel="Attribute Value"
          />
          <DoubleItemClipboardCopy
            leftValue="email"
            rightValue="LoginUser.Email"
            leftLabel="Attribute Name"
            rightLabel="Attribute Value"
          />
        </>
      ),
    },
  ];

  return (
    <Step
      title="Step 4: Configure Attribute Mapping"
      instructionList={instructionList}
    />
  );
}
