import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/okta/saml";
import { ClipboardCopyComponent } from "@wizardComponents";

interface Props {
  ssoUrl: string;
  audienceUri: string;
}

export const Step2: FC<Props> = ({ ssoUrl, audienceUri }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Single Sign-on URL"
          initialValue={ssoUrl}
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Audience URI (SP Entity ID)"
          initialValue={audienceUri}
        />
      ),
    },
    {
      text: 'Submit the "Single sign on URL" and the "Audience URI".',
      component: <StepImage src={Images.OktaSaml4} alt="Step 2.3" />,
    },
  ];

  return (
    <Step
      title="Step 2: Enter Service Provider Details"
      instructionList={instructions}
    />
  );
};
