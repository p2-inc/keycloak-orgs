import React, { FC } from "react";
import SalesforceCommonStep0Image from "@app/images/salesforce/COMMON/salesforce-0.png";
import SalesforceCommonStep1Image from "@app/images/salesforce/COMMON/salesforce-1.png";
import SalesforceCommonStep2Image from "@app/images/salesforce/COMMON/salesforce-2.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

type Props = {
  stepNumber: number;
};

export const SalesforceStepConnectedApp: FC<Props> = ({ stepNumber }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the Salesforce Console, open the <b>Setup</b> menu and select{" "}
          <b>App Manager</b> under <b>Apps</b>. Click <b>New Connected App</b>.
        </div>
      ),
      component: <StepImage src={SalesforceCommonStep0Image} alt={`Step ${stepNumber}.1`} />,
    },
    {
      text: (
        <div>
          Select <b>Create a Connected App</b>, then click <b>Continue</b>.
        </div>
      ),
      component: <StepImage src={SalesforceCommonStep1Image} alt={`Step ${stepNumber}.2`} />,
    },
    {
      text: (
        <div>
          Under the <b>Basic Information</b> section, enter a <b>Connected App Name</b>, <b>API Name</b>, and <b>Contact Email</b>.
          The <b>API Name</b> will be automatically populated based on the <b>Connected App Name</b>.
        </div>
      ),
      component: <StepImage src={SalesforceCommonStep2Image} alt={`Step ${stepNumber}.3`} />,
    },
  ];

  return (
    <Step
      title={`Step ${stepNumber}: Create Connected App`}
      instructionList={instructions}
    />
  );
}
