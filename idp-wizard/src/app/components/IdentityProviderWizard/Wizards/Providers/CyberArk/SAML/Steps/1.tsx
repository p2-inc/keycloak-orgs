import React from "react";
import * as Images from "@app/images/cyberark/SAML";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function CyberArkStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the CyberArk Identity Administration application, select{" "}
          <b>Web Apps</b> from the sidebar menu, then select <b>Add Web Apps</b>.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml0} alt="Step 1.1" />,
    },
    {
      text: (
        <div>
          Switch to the <b>Custom</b> tab, then click the <b>Add</b> button next
          to the <b>SAML</b> app template. Click <b>Yes</b> to add the SAML app template, 
          then close the popup dialog to view the new app.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml1} alt="Step 1.2" />,
    },
    {
      text: (
        <div>
          Provide a <b>Name</b> and <b>Category</b>{" "}
          for the application, then click <b>Save</b>.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml2} alt="Step 1.3" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Web App"
      instructionList={instructions}
    />
  );
}
