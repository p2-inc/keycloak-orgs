import React from "react";
import * as Images from "@app/images/duo/saml";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";

export function DuoStepFive() {
  const hostname = useHostname();
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>Settings</b> section, provide a <b>Name</b> for the
          application that is indicative of its function.
        </div>
      ),
      component: <StepImage src={Images.DuoSaml5} alt="Step 5.1" />,
    },
    {
      text: "Scroll all the way to the bottom of the window and click Save.",
      component: <></>,
    },
  ];

  return (
    <Step
      title="Step 5: Assign People & Groups"
      instructionList={instructionList}
    />
  );
}
