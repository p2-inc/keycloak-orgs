import React, { FC } from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
} from "@wizardComponents";

export const Step3: FC = () => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Attribute, or SAML Claims mapping allows the information about the
          user to be correctly mapped from the source identity provider to{" "}
          <code>demo.phasetwo.io</code>. The values on the left will be specific
          to your identity provider.
        </div>
      ),
      component: (
        <>
          <DoubleItemClipboardCopy
            leftValue="IdP username"
            rightValue="username"
          />
          <DoubleItemClipboardCopy leftValue="IdP email" rightValue="email" />
          <DoubleItemClipboardCopy
            leftValue="IdP first name"
            rightValue="firstName"
          />
          <DoubleItemClipboardCopy
            leftValue="IdP last name"
            rightValue="lastName"
          />
        </>
      ),
    },
  ];

  return (
    <Step
      title="Step 3: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
