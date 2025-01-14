import React from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/lastpass";

export function LastPassStepFive() {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>Custom Attributes</b> section, provide the following attribute
          mappings. Click <b>Add</b> at the bottom of the section to add a new mapping.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml7} alt="Step 5.1" />,
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy
            leftValue="Email Address"
            rightValue="email"
            leftLabel="Type"
            rightLabel="Name"
          />
          <DoubleItemClipboardCopy
            leftValue="First Name"
            rightValue="firstName"
            leftLabel="Type"
            rightLabel="Name"
          />
          <DoubleItemClipboardCopy
            leftValue="Last Name"
            rightValue="lastName"
            leftLabel="Type"
            rightLabel="Name"
          />
        </>
      ),
    },
    {
      text: (
        <div>
          Click the <b>Save</b> button at the bottom of the page to save your application configuration.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml8} alt="Step 5.2" />,
    },
  ];

  return (
    <Step
      title="Step 5: Configure Attribute Mapping"
      instructionList={instructionList}
    />
  );
}
