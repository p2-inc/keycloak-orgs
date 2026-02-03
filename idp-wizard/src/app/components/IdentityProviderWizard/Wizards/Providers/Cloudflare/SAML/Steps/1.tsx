import React from "react";
import * as Images from "@app/images/cloudflare/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function CloudflareStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In your Cloudflare Zero Trust dashboard, select{" "}
          <b>Access</b> and <b>Applications</b> from the sidebar menu, then
          choose <b>Add an application</b>.
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml0} alt="Step 1.1" />,
    },
    {
      text: (
        <div>
          On the <b>Add an application</b> page, select <b>SaaS</b> as the application type.
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml1} alt="Step 1.2" />,
    },
    {
      text: (
        <div>
          In the <b>Basic Information</b> section, enter a display name for the{" "}
          application. Set the authentication protocol to <b>SAML</b> by clicking the{" "}
          <b>Select SAML</b> button, then click <b>Add Application</b>.
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml2} alt="Step 1.3" />,
    }
  ];

  return (
    <Step
      title="Step 1: Create SaaS Application"
      instructionList={instructions}
    />
  );
}
