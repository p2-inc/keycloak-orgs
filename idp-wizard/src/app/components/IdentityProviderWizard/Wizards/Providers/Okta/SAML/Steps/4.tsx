import React, { FC } from "react";
import {
  DoubleItemClipboardCopy,
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import * as Images from "@app/images/okta/saml";

interface Props {}

export const Step4: FC<Props> = () => {
  const instructions: InstructionProps[] = [
    {
      text: 'Okta requires customer feedback. Select the option "I’m an Okta customer adding an internal app", click "Finish" and then leave the additional form blank.',
      component: <StepImage src={Images.OktaSaml6} alt="Step 4.1" />,
    },
  ];

  return (
    <Step
      title="Step 4: Complete Feedback Section"
      instructionList={instructions}
    />
  );
};
