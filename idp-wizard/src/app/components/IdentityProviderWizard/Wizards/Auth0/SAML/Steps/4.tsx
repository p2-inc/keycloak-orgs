import React from "react";
import Auth0Step5Image from "@app/images/auth0/SAML/auth0-5SAML.png";
import Auth0Step6Image from "@app/images/auth0/SAML/auth0-6SAML.png";
import {
  InstructionProps,
  Step,
  ClipboardCopyComponent,
  StepImage,
} from "@wizardComponents";

type Props = {
  loginRedirectURL: string;
};

export const Auth0StepFour: React.FC<Props> = ({ loginRedirectURL }) => {
  const instructions: InstructionProps[] = [
    
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this Application Callback URL"
          initialValue={loginRedirectURL}
        />
      ),
    },
    {
      text: "In the “Settings” section of the popup, enter the Application Callback URL.",
      component: <StepImage src={Auth0Step5Image} alt="Step 4.1" />,
    },
    {
      text: "Scroll all the way to the bottom of this popup and click “Enable”.",
      component: <StepImage src={Auth0Step6Image} alt="Step 4.2" />,
    },
  ];

  return (
    <Step
      title="Step 4: Enter Application Callback URL"
      instructionList={instructions}
    />
  );
};
