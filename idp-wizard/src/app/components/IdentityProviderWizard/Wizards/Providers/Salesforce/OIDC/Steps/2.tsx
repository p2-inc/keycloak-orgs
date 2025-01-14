import React from "react";
import SalesforceOidcStep0Image from "@app/images/salesforce/OIDC/salesforce_oidc_0.png";
import SalesforceOidcStep1Image from "@app/images/salesforce/OIDC/salesforce_oidc_1.png";
import {
  InstructionProps,
  Step,
  ClipboardCopyComponent,
  StepImage,
} from "@wizardComponents";

type Props = {
  loginRedirectURL: string;
};

export const SalesforceStepTwo: React.FC<Props> = ({ loginRedirectURL }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          Under the <b>API (Enable OAuth Settings)</b> section, check the <b>Enable OAuth Settings</b>{" "}
          checkbox and paste the URL from below into the <b>Callback URL</b> field.
        </div>
      ),
      component: <StepImage src={SalesforceOidcStep0Image} alt="Step 2.1" />,
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy this login Callback URL"
          initialValue={loginRedirectURL}
        />
      ),
    },
    {
      text: (
        <div>
          Select the <b>Access the identity URL service (id, profile, email, address, phone)</b> and{" "}
          <b>Access unique user identifiers (openid)</b> OAuth Scopes, then click the <b>Add</b> button.{" "}
          <br/><br/>
          Uncheck the <b>Require Proof Key for Code Exchange (PKCE) Extension for Supported Authorization Flows</b> checkbox.
        </div>
      ),
      component: <StepImage src={SalesforceOidcStep1Image} alt="Step 2.2" />,
    },
    {
      component: (
        <div>
          Click the <b>Save</b> button at the bottom of the page to save your changes.
        </div>
      ),
    }
  ];

  return (
    <Step
      title="Step 2: Configure OAuth Settings"
      instructionList={instructions}
    />
  );
};
