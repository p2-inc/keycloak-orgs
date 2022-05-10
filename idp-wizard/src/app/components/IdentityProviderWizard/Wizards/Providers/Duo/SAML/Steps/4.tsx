import React from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/duo/saml";

interface IClaims {
  name: string;
  value: string;
}

export function DuoStepFour() {
  const claimNames: IClaims[] = [
    {
      name: "Username",
      value: "id",
    },
    {
      name: "Email address",
      value: "email",
    },
    {
      name: "First Name",
      value: "firstName",
    },
    {
      name: "Last Name",
      value: "lastName",
    },
  ];

  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>SAML Response</b> section, provide the following attribute
          mappings in the <b>Map attributes</b> section. Click <b>+</b> next to
          each field to add a new mapping.
        </div>
      ),
      component: <StepImage src={Images.DuoSaml3} alt="Step 4.1" />,
    },
    {
      text: "Copy the following IdP attributes and SAML response attributes",
      component: <StepImage src={Images.DuoSaml4} alt="Step 4.2" />,
    },
    {
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
  ];

  return (
    <Step
      title="Step 4: Configure Attribute Mapping"
      instructionList={instructionList}
    />
  );
}
