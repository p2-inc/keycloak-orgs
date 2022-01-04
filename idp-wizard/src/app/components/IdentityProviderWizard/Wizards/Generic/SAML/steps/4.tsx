import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";

export const Step4: FC = () => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          It may be necessary to enable your new SAML application, and associate
          users and groups with it in order for your users to be able to log in
          to <code>demo.phasetwo.io</code>. Continue when you have configured
          user access for your new SAML application in your identity provider.
        </div>
      ),
    },
  ];

  return (
    <Step
      title="Step 4: Create Enterprise Application"
      instructionList={instructions}
    />
  );
};
