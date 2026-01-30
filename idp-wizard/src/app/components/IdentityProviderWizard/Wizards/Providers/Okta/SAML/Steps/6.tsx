import React, { FC } from "react";
import { FileCard, InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/okta/saml";

import { API_RETURN_PROMISE } from "@app/configurations/api-status";
import { FileText } from "@app/components/IdentityProviderWizard/Wizards/components/forms";

type Props = {
  handleFormSubmit: ({
    idpMetadata,
  }: {
    idpMetadata: string;
  }) => API_RETURN_PROMISE;
};

export const Step6: FC<Props> = ({ handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'In the "Sign On" section, click the "View SAML setup instructions" link.',
      component: <StepImage src={Images.OktaSaml8} alt="Step 6.1" />,
    },
    {
      text: 'A second window will open with the SAML setup instructions. Find the "Optional" section with the textbox containing the identity provider metadata. Copy the entire contents of the text box and paste it below.',
      component: <StepImage src={Images.OktaSaml9} alt="Step 6.1" />,
    },
    {
      component: (
        <FileCard title="Validate Identity Provider Metadata">
          <FileText handleFormSubmit={handleFormSubmit} />
        </FileCard>
      ),
    },
  ];

  return (
    <Step
      title="Step 6: Upload Okta Identity Provider Information"
      instructionList={instructions}
    />
  );
};
