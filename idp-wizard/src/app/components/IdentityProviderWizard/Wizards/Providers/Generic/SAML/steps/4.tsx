import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";

export const Step4: FC = () => {
  const hostname = useHostname();
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          It may be necessary to enable your new SAML application, and associate
          users and groups with it in order for your users to be able to log in
          to <code>{hostname}</code>. Continue when you have configured user
          access for your new SAML application in your identity provider.
        </div>
      ),
    },
  ];

  return (
    <Step
      title="Step 4: Configure User Access"
      instructionList={instructions}
    />
  );
};
