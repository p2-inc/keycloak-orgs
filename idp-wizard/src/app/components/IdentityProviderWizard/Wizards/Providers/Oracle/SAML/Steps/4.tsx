import React from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/oracle/SAML";

interface IClaims {
  name: string;
  value: string;
}

export function OracleStepFour() {
  const claimNames: IClaims[] = [
    {
      name: "firstName",
      value: "First name",
    },
    {
      name: "lastName",
      value: "Last name",
    },
    {
      name: "email",
      value: "Primary email",
    },
    {
      name: "username",
      value: "User Name",
    },
  ];

  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Scroll down to the <b>Attribute configuration</b> section and add the
          following attribute mappings. Click <b>+ Additional attribute</b>{" "}
          at the bottom of the section to add each attribute.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml8} alt="Step 4.1" />,
    },
    {
      text: "Copy the following attribute names and type values.",
      component: <StepImage src={Images.OracleSaml9} alt="Step 4.2" />,
    },
    {
      component: claimNames.map(
        ({ name: leftValue, value: rightValue }, index) => (
          <DoubleItemClipboardCopy
            leftValue={leftValue}
            rightValue={rightValue}
            leftLabel="Name"
            rightLabel="Type Value"
            key={index}
          />
        )
      ),
    },
    {
      component: (
        <div>
          Click <b>Finish</b> button to complete the configuration.
        </div>
      ),
    }
  ];

  return (
    <Step
      title="Step 4: Configure Attribute Mapping"
      instructionList={instructionList}
    />
  );
}
