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
  FileUpload,
  Form,
  FormGroup,
  FileUploadFieldProps,
  FormAlert,
  Alert,
} from "@patternfly/react-core";

interface Step3Props {
  uploadMetadataFile: (file: File) => Promise<boolean>;
}

export const Auth0StepThree: FC<Step3Props> = ({ uploadMetadataFile }) => {
  const [metadataFileValue, setMetadataFileValue] = useState("");
  const [metadataFileName, setMetadataFileName] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [uploadValid, setUploadValid] = useState<boolean | null>(null);

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
            <Form>
              {uploadValid && (
                <FormAlert>
                  <Alert
                    variant="success"
                    title="Config uploaded successfully. Please continue."
                    aria-live="polite"
                    isInline
                  />
                </FormAlert>
              )}
              {uploadValid === false && (
                <FormAlert>
                  <Alert
                    variant="danger"
                    title="Config not uploaded successfully. Please check the file and try again."
                    aria-live="polite"
                    isInline
                  />
                </FormAlert>
              )}
              <FormGroup
                label="Metadata File"
                isRequired
                fieldId="metadata-file"
                className="form-label"
              >
                <FileUpload
                  id="metadata-file"
                  value={metadataFileValue}
                  filename={metadataFileName}
                  filenamePlaceholder="Drop or choose metadata file .xml to upload."
                  browseButtonText="Select"
                  onChange={handleFileInputChange}
                  dropzoneProps={{
                    accept: "text/xml",
                  }}
                  isLoading={isUploading}
                />
              </FormGroup>
            </Form>
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
