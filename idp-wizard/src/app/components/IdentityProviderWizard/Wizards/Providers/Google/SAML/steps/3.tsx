import React, { FC } from "react";
import {
  FileCard,
  InstructionProps,
  Step,
  StepImage,
  MetadataFile,
} from "@wizardComponents";
import * as Images from "@app/images/google";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

interface Step3Props {
  handleFormSubmit: ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => API_RETURN_PROMISE;
}

export const Step3: FC<Step3Props> = ({ handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'Download the metadata file, and click "Continue".',
      component: <StepImage src={Images.GoogleSaml3} alt="Step 3.1" />,
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
      title="Step 3: Upload Google IdP Information"
      instructionList={instructions}
    />
  );
};
