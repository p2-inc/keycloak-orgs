import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  DoubleItemClipboardCopy,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/google";

interface Props {}

export const Step5: FC<Props> = () => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <>
          <div className="pf-u-mb-md">
            Provide the following Attribute Mappings and select "Finish"
          </div>
          <DoubleItemClipboardCopy
            leftValue="Primary email"
            rightValue="email"
          />
          <DoubleItemClipboardCopy
            leftValue="First name"
            rightValue="firstName"
          />
          <DoubleItemClipboardCopy
            leftValue="Last name"
            rightValue="lastName"
          />
        </>
      ),
    },
    {
      component: <StepImage src={Images.GoogleSaml5} alt="Step 5.1" />,
    },
  ];

  return (
    <Step
      title="Step 5: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
