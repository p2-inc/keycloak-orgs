import React from "react";
import SalesforceSamlStep3Image from "@app/images/salesforce/SAML/salesforce_saml_3.png";
import SalesforceSamlStep4Image from "@app/images/salesforce/SAML/salesforce_saml_4.png";
import {
  InstructionProps,
  Step,
  DoubleItemClipboardCopy,
  StepImage,
} from "@wizardComponents";

export function SalesforceStepSix() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Under the <b>Custom Attributes</b> section, provide the following attribute{" "}
          mappings by selecting <b>New</b> for each attribute listed below.
        </div>
      ),
      component: <StepImage src={SalesforceSamlStep3Image} alt="Step 6.1" />,
    },
    {
      text: (
        <div>
          Copy the <b>Attribute Key</b> and <b>Attribute Value</b> for each attribute into the{" "}
          respective fields.
        </div>
      ),
      component: <StepImage src={SalesforceSamlStep4Image} alt="Step 6.2" />,
    },
    {
      component: (
        <>
          <DoubleItemClipboardCopy
            leftValue="firstName"
            rightValue="$User.FirstName"
            leftLabel="Attribute Key"
            rightLabel="Attribute Value"
          />
          <DoubleItemClipboardCopy
            leftValue="lastName"
            rightValue="$User.LastName"
            leftLabel="Attribute Key"
            rightLabel="Attribute Value"
          />
        </>
      ),
    },
  ];

  return (
    <Step
      title="Step 6: Configure Attribute Mapping"
      instructionList={instructions}
    />
  );
};
