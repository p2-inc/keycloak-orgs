import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  DoubleItemClipboardCopy,
} from "@wizardComponents";
import * as Images from "@app/images/onelogin";

export const Step3: FC = () => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          In the “Parameters” section, provide the following attribute mappings
          and select "Save".
        </div>
      ),
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy leftValue="UUID" rightValue="id" />
          <DoubleItemClipboardCopy rightValue="Email" leftValue="email" />
          <DoubleItemClipboardCopy
            rightValue="First name"
            leftValue="firstName"
          />
          <DoubleItemClipboardCopy
            rightValue="Last name"
            leftValue="lastName"
          />
        </>
      ),
    },
    {
      text: "Note you may need to click the “+” to configure each of the mappings.",
      component: <StepImage src={Images.OneLogin_SAML_5} alt="Step 3.1" />,
    },
    {
      text: "Note that you must select “Include in SAML assertion” when creating each of the mappings.",
      component: <StepImage src={Images.OneLogin_SAML_5A} alt="Step 3.2" />,
    },
  ];

  return (
    <Step
      title="Step 3: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
