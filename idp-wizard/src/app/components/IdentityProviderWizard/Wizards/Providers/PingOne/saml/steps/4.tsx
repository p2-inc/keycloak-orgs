import React, { FC } from "react";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/pingone";
import { MetadataFile } from "@app/components/IdentityProviderWizard/Wizards/components";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  handleFormSubmit: ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => API_RETURN_PROMISE;
};

export const Step4: FC<Props> = ({ handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'Once you have completed the PingOne setup, you will be presented with a summary of the provider configuration. Select the "Configuration" tab and click "Download Metadata"',
      component: <StepImage src={Images.PINGONE_SAML_7} alt="Step 4.1" />,
    },
    {
      component: <MetadataFile handleFormSubmit={handleFormSubmit} />,
    },
  ];

  return (
    <Step
      title="Step 4: Upload PingOne IdP Information"
      instructionList={instructions}
    />
  );
};
