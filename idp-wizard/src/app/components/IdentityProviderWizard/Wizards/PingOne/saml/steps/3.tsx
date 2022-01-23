import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  DoubleItemClipboardCopy,
} from "@wizardComponents";
import * as Images from "@app/images/pingone";

export const Step3: FC = () => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          Provide the following Attribute Mappings and select "Save and Close".
          Note you may need to click “Add Attribute” to configure each of the
          mappings.
        </div>
      ),
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy leftValue="id" rightValue="User ID" />
          <DoubleItemClipboardCopy
            leftValue="email"
            rightValue="Email Address"
          />
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="Given name"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="Family name"
          />
        </>
      ),
    },
    {
      component: <StepImage src={Images.PINGONE_SAML_6} alt="Step 3.1" />,
    },
  ];

  return (
    <Step
      title="Step 3: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
