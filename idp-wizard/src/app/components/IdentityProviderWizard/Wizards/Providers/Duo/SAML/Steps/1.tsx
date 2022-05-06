import React from "react";
import * as Images from "@app/images/duo/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function DuoStepOne() {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In your Duo Security administrative application, select{" "}
          <b>Applications</b>
          and <b>Protect an application</b> from the sidebar menu, and then
          search for <b>Generic Service Provider</b>. Select this application by
          clicking the <b>Protect</b> button.
        </div>
      ),
      component: <StepImage src={Images.DuoSaml0} alt="Step 1.1" />,
    },
  ];

  return (
    <Step
      title="Step 1: Create Enterprise Application"
      instructionList={instructions}
    />
  );
}
