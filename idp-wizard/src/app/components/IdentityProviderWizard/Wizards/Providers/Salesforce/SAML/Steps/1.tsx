import React from "react";
import SalesforceSamlStep0Image from "@app/images/salesforce/SAML/salesforce_saml_0.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function SalesforceStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the Salesforce Console, open the <b>Setup</b> menu and select{" "}
          <b>Identity Provider</b> under <b>Identity</b>. Verify that your{" "}
          Salesforce identity provider has been enabled, or click <b>Enable{" "}
          Identity Provider</b> to enable it.
        </div>
      ),
      component: <StepImage src={SalesforceSamlStep0Image} alt="Step 1.1" />,
    },
  ];

  return (
    <Step
      title="Step 1: Enable Identity Provider"
      instructionList={instructions}
    />
  );
}
