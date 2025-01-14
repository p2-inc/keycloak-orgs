import React, { FC } from "react";
import * as Images from "@app/images/oracle/SAML";
import {
  FileCard,
  InstructionProps,
  Step,
  StepImage,
  MetadataFile,
} from "@wizardComponents";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  handleFormSubmit: ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => API_RETURN_PROMISE;
};

export const OracleStepTwo: FC<Props> = ({ handleFormSubmit }) => {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Click the <b>Download identity provider metadata</b> button,
          then upload the metadata file in the form below.
        </div>
      ),
      component: <StepImage src={Images.OracleSaml5} alt="Step 2.1" />,
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
      title="Step 2: Upload SAML Metadata"
      instructionList={instructionList}
    />
  );
};
