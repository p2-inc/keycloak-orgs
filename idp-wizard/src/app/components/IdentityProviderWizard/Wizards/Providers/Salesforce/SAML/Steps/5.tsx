import React from "react";
import SalesforceCommonStep4Image from "@app/images/salesforce/COMMON/salesforce-4.png";
import SalesforceCommonStep5Image from "@app/images/salesforce/COMMON/salesforce-5.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function SalesforceStepFive() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Under the <b>Profiles</b> section, click <b>Manage Profiles</b> to assign the connected app to the appropriate profiles.
        </div>
      ),
      component: <StepImage src={SalesforceCommonStep4Image} alt={`Step 5.1`} />,
    },
    {
      text: (
        <div>
          Select the desired profiles then click <b>Save</b> at the bottom of the page.
        </div>
      ),
      component: <StepImage src={SalesforceCommonStep5Image} alt={`Step 5.2`} />,
    }
  ];

  return (
    <Step
      title={`Step 5: Assign Profiles`}
      instructionList={instructions}
    />
  );
}
