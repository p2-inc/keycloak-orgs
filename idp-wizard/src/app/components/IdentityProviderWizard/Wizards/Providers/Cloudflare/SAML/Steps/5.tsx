import React from "react";
import * as Images from "@app/images/cloudflare/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";

export function CloudflareStepFive() {
  const hostname = useHostname();
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Set a name for the access policy, then add any groups or rules defining who can access the application.
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml6} alt="Step 5.1" />,
    },
    {
      text: (
        <div>
          Click <b>Done</b> to finish creating the access policy.
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml7} alt="Step 5.2" />,
    }
  ];

  return (
    <Step
      title="Step 5: Assign Access Policy"
      instructionList={instructionList}
    />
  );
}
