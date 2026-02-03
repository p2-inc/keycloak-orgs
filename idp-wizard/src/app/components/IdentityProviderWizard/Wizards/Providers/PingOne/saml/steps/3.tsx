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
          Provide the following Attribute Mappings and select "Save". The "saml_subject"
	  mapping that exists will have to be edited to use "Username" as its outgoing value.
          Note you may need to click “Add Attribute” to configure each of the
          mappings.
        </div>
      ),
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy rightValue="User ID" leftValue="saml_subject" />
          <DoubleItemClipboardCopy rightValue="Username" leftValue="username" />
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
