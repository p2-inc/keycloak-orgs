import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
} from "@wizardComponents";

type Props = {
  redirectUri: string;
};

export const Step1: FC<Props> = ({ redirectUri }) => {
  const instructions: InstructionProps[] = [
    {
      text: "In your identity provider, create an OpenID Connect (OIDC) Application. If you are given the option, choose to create a “confidential” or “web” application. You may need the following information in order to create and configure your application. Don’t worry if you are not asked for these, as not all providers require everything.",
      component: (
        <ClipboardCopyComponent
          label="Login Redirect URI"
          helperText="Copy the login redirect URI"
          initialValue={redirectUri}
        />
      ),
    },
  ];

  return (
    <Step
      title="Step 1: Create an OpenID Connect Application"
      instructionList={instructions}
    />
  );
};
