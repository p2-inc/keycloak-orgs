import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/pingone";

type Props = {
  acsUrl: string;
  entityId: string;
};

export const Step2: FC<Props> = ({ acsUrl, entityId }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this ACS URL"
            initialValue={acsUrl}
          />
          <ClipboardCopyComponent
            label="Copy this Entity ID"
            initialValue={entityId}
          />
        </>
      ),
    },
    {
      text: 'Select "Manually Enter", and submit the "ACS URL" and the "Entity ID". Click "Save" once both values are pasted.',
      component: <StepImage src={Images.PINGONE_SAML_4} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 2: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
