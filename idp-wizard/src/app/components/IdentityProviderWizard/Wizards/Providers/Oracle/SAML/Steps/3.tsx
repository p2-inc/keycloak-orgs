import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/oracle/SAML";

interface Props {
  acsUrl: string;
  entityId: string;
}

export const OracleStepThree: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the <b>General</b> section, paste the <b>Entity ID</b> and the{" "}
          <b>Assertion consumer URL</b> from below into the appropriate fields.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml6} alt="Step 3.1" />,
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Entity ID"
          initialValue={entityId}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Assertion consumer URL"
          initialValue={acsUrl}
        />
      ),
    },
    {
      text: (
        <div>
          In the <b>Additional configurations</b> section, uncheck the{" "}
          <b>Enable single logout</b> checkbox.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml7} alt="Step 3.2" />,
    }
  ];

  return (
    <Step
      title="Step 3: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
