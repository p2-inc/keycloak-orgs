import React from "react";
import Auth0Step4Image from "@app/images/auth0/auth0-4.png";
import {
  InstructionProps,
  Step,
  ClipboardCopyComponent,
  StepImage,
} from "@wizardComponents";

type Props = {
  loginRedirectURL: string;
};

export const Auth0StepThree: React.FC<Props> = ({ loginRedirectURL }) => {
  const instructions: InstructionProps[] = [
    {
      text: "On the same page in your new app, scroll down to the “Application URIs” section, and paste the value below into the field named “Allowed Callback URLs”. Don’t forget to scroll down to the bottom and click “Save Changes”.",
      component: <StepImage src={Auth0Step4Image} alt="Step 3.1" />,
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this login redirect URI"
          initialValue={loginRedirectURL}
        />
      ),
    },
  ];

  return (
    <Step
      title="Step 3: Configure Redirect URI"
      instructionList={instructions}
    />
  );
};
