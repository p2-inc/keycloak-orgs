import React, { FC } from "react";
import Auth0Step4Image from "@app/images/auth0/SAML/auth0-4SAML.png";
import { FileCard, InstructionProps, Step, StepImage } from "@wizardComponents";
import { API_RETURN } from "@app/configurations/api-status";
import { MetadataFile } from "@app/components/IdentityProviderWizard/Wizards/components";

interface Step3Props {
  uploadMetadataFile: (file: File) => Promise<API_RETURN>;
}

export const Auth0StepThree: FC<Step3Props> = ({ uploadMetadataFile }) => {
  const handleMetadataFileValidation = async ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => {
    const resp = await uploadMetadataFile(metadataFile);
    return resp;
  };

  const instructions: InstructionProps[] = [
    {
      text: "In the “Usage” section of the popup, click “Download” next to the “Identity Provider Metadata”.",
      component: <StepImage src={Auth0Step4Image} alt="Step 3.1" />,
    },
    {
      component: (
        <FileCard>
          <MetadataFile
            handleFormSubmit={handleMetadataFileValidation}
            formActive={true}
          />
        </FileCard>
      ),
    },
  ];

  return (
    <Step
      title="Step 3: Upload Auth0 IdP Information"
      instructionList={instructions}
    />
  );
};
