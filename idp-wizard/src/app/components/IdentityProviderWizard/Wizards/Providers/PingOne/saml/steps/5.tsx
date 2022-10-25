import React, { FC } from "react";
import {
  FileCard,
  InstructionProps,
  Step,
  StepImage,
  MetadataFile,
} from "@wizardComponents";
import * as Images from "@app/images/pingone";
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
      text: 'Once you have completed the PingOne setup, you will be presented with a summary of the provider configuration. Enable the application for your users by selecting the toggle switch in the upper righthand corner. Select the "Configuration" tab and click "Download Metadata", and upload that file below.',
      component: <StepImage src={Images.PINGONE_SAML_7} alt="Step 5.1" />,
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
      title="Step 5: Upload PingOne IdP Information"
      instructionList={instructions}
    />
  );
};
