import React from "react";
import * as Images from "@app/images/oracle/SAML";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function OracleStepFive() {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Click the <b>Activate</b> button to enable the application.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml10} alt="Step 5.1" />,
    },
    {
      text: (
        <div>
          Select <b>Groups</b> or <b>Users</b> from the sidebar menu, then click{" "}
          <b>Assign groups</b> or <b>Assign users</b> to grant access to the application.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml11} alt="Step 5.2" />,
    },
  ];

  return (
    <Step
      title="Step 5: Assign Users & Groups"
      instructionList={instructionList}
    />
  );
}
