import React from "react";
import * as Images from "@app/images/oracle/SAML";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function OracleStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the Oracle Cloud Infrastructure console, open the navigation menu
          and navigate to <b>Domains</b> under <b>Identity &amp; Security</b>.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml0} alt="Step 1.1" />,
    },
    {
      text: (
        <div>
          Select the domain you would like to add the application to.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml1} alt="Step 1.2" />,
    },
    {
      text: (
        <div>
          In the sidebar menu, select <b>Integrated Applications</b>, then
          click <b>Add application</b>.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml2} alt="Step 1.3" />,
    },
    {
      text: (
        <div>
          Choose <b>SAML Application</b>, then select <b>Launch Workflow</b>.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml3} alt="Step 1.4" />,
    },
    {
      text: (
        <div>
          Enter a <b>Name</b> for your application, then click <b>Next</b> at the
          bottom of the page.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml4} alt="Step 1.5" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create SAML Application"
      instructionList={instructions}
    />
  );
}
