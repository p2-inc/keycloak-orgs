import React, { FC, useState } from "react";
import Auth0Step4Image from "@app/images/auth0/SAML/auth0-4SAML.png";
import {
  InstructionProps,
  Step,
  StepImage,
} from "@wizardComponents";
import {
  Card,
  CardBody,
  CardTitle,
  FileUploadFieldProps,
} from "@patternfly/react-core";
import {
  API_RETURN,
} from "@app/configurations/api-status";
import { MetadataFile } from "./forms";

interface Step3Props {
  uploadMetadataFile: (file: File) => Promise<API_RETURN>;
}

export const Auth0StepThree: FC<Step3Props> = ({ uploadMetadataFile }) => {
  const [metadataFileValue, setMetadataFileValue] = useState("");
  const [metadataFileName, setMetadataFileName] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [uploadValid, setUploadValid] = useState<boolean | null>(null);
/*
  const handleFileInputChange: FileUploadFieldProps["onChange"] = async (
    value,
    filename,
    event
  ) => {
    setMetadataFileName(filename);
    setMetadataFileValue(value);
    setUploadValid(null);

    if (!value) return;

    setIsUploading(true);

    const uploadStatus = await uploadMetadataFile(value);

    if (uploadStatus) {
      setUploadValid(true);
    } else {
      setUploadValid(false);
    }

    setIsUploading(false);
  };*/

  
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
    },,
    {
      component: (
        <Card isFlat>
          <CardTitle>Upload metadata file</CardTitle>
          <CardBody>
            <MetadataFile
              handleFormSubmit={handleMetadataFileValidation}
              formActive={true}
            />

          </CardBody>
        </Card>
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
