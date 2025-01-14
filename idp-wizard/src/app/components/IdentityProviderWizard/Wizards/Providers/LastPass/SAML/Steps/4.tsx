import React, { FC } from "react";
import * as Images from "@app/images/lastpass";
import {
  FileCard,
  InstructionProps,
  Step,
  StepImage,
  MetadataFile,
} from "@wizardComponents";
import { API_RETURN } from "@app/configurations/api-status";

interface Props {
  uploadMetadataFile: (file: File) => Promise<API_RETURN>;
}

export const LastPassStepFour: FC<Props> = ({ uploadMetadataFile }) => {
  const handleMetadataFileValidation = async ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => {
    const resp = await uploadMetadataFile(metadataFile);
    return resp;
  };

  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Click the <b>Export SAML IdP Metadata</b> button,{" "}
          then select <b>Download</b> to save the metadata file.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml5} alt="Step 4.1" />,
    },
    {
      text: (
        <div>
          Select <b>Download</b> to save the metadata file.
        </div>
      ),
      component: <StepImage src={Images.LastPassSaml6} alt="Step 4.2" />,
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
      title="Step 4: Upload SAML Metadata"
      instructionList={instructionList}
    />
  );
};
