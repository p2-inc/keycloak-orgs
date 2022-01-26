import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/aws";

type Props = {
  urls: {
    samlAudience: string;
    acsURL: string;
  };
};

export const Step3: FC<Props> = ({ urls: { samlAudience, acsURL } }) => {
  const instructions: InstructionProps[] = [
    {
      text: "Copy the following values.",
      component: (
        <>
          <ClipboardCopyComponent
            label="Copy this SAML audience"
            initialValue={samlAudience}
          />
          <ClipboardCopyComponent
            label="Copy this ACS URL"
            initialValue={acsURL}
          />
        </>
      ),
    },
    {
      text: 'Submit the "SAML audience" and the "ACS URL". Click "Save changes".',
      component: <StepImage src={Images.AWS_SSO_SAML_4} alt="Step 3.1" />,
    },
  ];

  return (
    <Step
      title="Step 3: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
