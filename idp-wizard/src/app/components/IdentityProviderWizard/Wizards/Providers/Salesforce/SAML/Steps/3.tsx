import React, { FC } from "react";
import SalesforceSamlStep1Image from "@app/images/salesforce/SAML/salesforce_saml_1.png";
import { InstructionProps, Step, StepImage, ClipboardCopyComponent } from "@wizardComponents";

type Props = {
  acsUrl: string;
  entityId: string;
};

export const SalesforceStepThree: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Under <b>Web App Settings</b>, check the <b>Enable SAML</b> checkbox, then{" "}
          paste the <b>Entity Id</b> and <b>ACS URL</b> into the appropriate fields.
        </div>
      ),
      component: <StepImage src={SalesforceSamlStep1Image} alt="Step 3.1" />,
    },
    {
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this Entity Id"
            initialValue={entityId}
          />
          <ClipboardCopyComponent
            label="Copy this ACS URL"
            initialValue={acsUrl}
          />
        </>
      ),
    },
    {
      component: (
        <div>
          Click the <b>Save</b> button at the bottom of the page to save your changes.
        </div>
      ),
    }
  ];

  return (
    <Step
      title="Step 3: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
}
