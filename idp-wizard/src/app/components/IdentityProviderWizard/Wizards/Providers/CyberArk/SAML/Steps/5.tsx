import React from "react";
import * as Images from "@app/images/cyberark/SAML";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function CyberArkStepFive() {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Switch to the <b>Permissions</b> page, then click the <b>Add</b> button.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml6} alt="Step 5.1" />,
    },
    {
      text: (
        <div>
          Search for the users, groups, or roles you want to assign to the application,
          then click <b>Add</b>.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml7} alt="Step 5.2" />,
    },
    {
      component: (
        <div>
          Click the <b>Save</b> button at the bottom of the page to save your changes.
        </div>
      )
    },
  ];

  return (
    <Step
      title="Step 5: Assign Users, Groups, and Roles"
      instructionList={instructionList}
    />
  );
}
