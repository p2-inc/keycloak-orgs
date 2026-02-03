import React, { FC } from "react";
import {
  ClipboardCopyComponent,
  InstructionProps,
  Step,
} from "@wizardComponents";

interface Props {
  ssoUrl: string;
  entityId: string;
  samlMetadata: string;
}

export const Step1: FC<Props> = ({ ssoUrl, entityId, samlMetadata }) => {
  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          In your identity provider, create a SAML Application. You may need the
          following information in order to create and configure your
          application. Don't worry if you are not asked for these, as not all
          providers require everything.
        </div>
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy the ACS URL"
          initialValue={ssoUrl}
          helperText='Note that "sometimes called SSO Service URL"'
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy the Entity ID"
          initialValue={entityId}
          helperText='Note that sometimes called "Audience URI"'
        />
      ),
    },
    {
      component: (
        <ClipboardCopyComponent
          label="Copy the SAML Metadata"
          initialValue={samlMetadata}
          helperText='Note that "sometimes called Entity Provider Metadata or Descriptor"'
        />
      ),
    },
  ];

  return (
    <Step
      title="Step 1: Create a SAML Application"
      instructionList={instructions}
    />
  );
};
