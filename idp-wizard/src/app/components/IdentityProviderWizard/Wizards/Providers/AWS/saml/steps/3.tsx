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
            label="Copy this ACS URL"
            initialValue={acsURL}
          />
          <ClipboardCopyComponent
            label="Copy this SAML audience"
            initialValue={samlAudience}
          />
        </>
      ),
    },
    {
      text: 'Select the link "If you don\'t have a metadata file, you can manually type your metadata values.". Submit the "ACS URL" and the "SAML audience" values into the fields. Click "Save changes".',
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
