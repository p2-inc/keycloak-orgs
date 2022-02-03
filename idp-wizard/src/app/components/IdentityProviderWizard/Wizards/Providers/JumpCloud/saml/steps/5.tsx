import React, { FC } from "react";
import { FileCard, InstructionProps, Step, StepImage } from "@wizardComponents";
import * as Images from "@app/images/jumpcloud";
import { MetadataFile } from "@app/components/IdentityProviderWizard/Wizards/components";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  handleFormSubmit: ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => API_RETURN_PROMISE;
};

export const Step5: FC<Props> = ({ handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'Back in the list of applications, select the checkbox next to the application you just created. Select the "export metadata" button and note the file that is downloaded.',
      component: <StepImage src={Images.JumpCloud_SAML_10} alt="Step 5.1" />,
    },
    {
      component: (
        <FileCard>
          <MetadataFile handleFormSubmit={handleFormSubmit} />
        </FileCard>
      ),
    },
  ];

  return (
    <Step
      title="Step 5: Upload JumpCloud IdP Information"
      instructionList={instructions}
    />
  );
};
