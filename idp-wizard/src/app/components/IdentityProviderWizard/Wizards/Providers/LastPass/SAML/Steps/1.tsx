import React from "react";
import * as Images from "@app/images/lastpass";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function LastPassStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the LastPass Admin Console, select{" "}
          <b>Applications</b> from the menu bar, then select{" "}
          the <b>Search the catalog</b> or <b>Add app</b> buttons.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml0} alt="Step 1.1" />,
    },
    {
      text: (
        <div>
          Search for <b>Custom service</b> in the search bar, then select{" "}
          the <b>Custom service</b> tile.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml1} alt="Step 1.2" />,
    },
    {
      text: (
        <div>
          Select the <b>Add new domain</b> button to add a new SAML connection to the Custom service application.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml2} alt="Step 1.3" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Application"
      instructionList={instructions}
    />
  );
}
