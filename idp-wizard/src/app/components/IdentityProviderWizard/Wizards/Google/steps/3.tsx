import React, { FC, useState } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import * as Images from "@app/images/google";
import { useImageModal } from "@app/hooks/useImageModal";
import {
  Card,
  CardBody,
  CardTitle,
  FileUpload,
  Form,
  FormGroup,
  Modal,
  ModalVariant,
  FileUploadFieldProps,
  FormAlert,
  Alert,
} from "@patternfly/react-core";

interface Step3Props {
  uploadMetadataFile: (file: File) => Promise<boolean>;
}

export const Step3: FC<Step3Props> = ({ uploadMetadataFile }) => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();

  const [metadataFileValue, setMetadataFileValue] = useState();
  const [metadataFileName, setMetadataFileName] = useState();
  const [isUploading, setIsUploading] = useState(false);
  const [uploadValid, setUploadValid] = useState<boolean | null>(null);

  const handleFileInputChange: FileUploadFieldProps["onChange"] = async (
    value,
    filename,
    event
  ) => {
    setMetadataFileName(filename);
    setMetadataFileValue(value);
    setIsUploading(true);
    setUploadValid(null);

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
      text: 'Download the metadata file, and click "Continue".',
      component: (
        <img
          src={Images.GoogleSaml3}
          alt="Step 3.1"
          className="step-image"
          onClick={() => onImageClick(Images.GoogleSaml3)}
        />
      ),
    },
    {
      // text: "Upload metadata file.",
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
    <>
      <Modal
        aria-label="Image"
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Step
        title="Step 3: Upload Google IdP Information"
        instructionList={instructions}
      />
    </>
  );
};
