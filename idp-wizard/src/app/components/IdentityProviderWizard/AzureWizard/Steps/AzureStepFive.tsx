import {
  Card,
  CardBody,
  Title,
  FileUpload,
  Form,
  FormGroup,
  Stack,
  StackItem,
  Text,
  TextVariants,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import React, { useEffect, useState } from "react";
import azureStep13Image from "@app/images/azure/azure-13.png";

export function AzureStepFive() {
  const [value, setValue] = useState();
  const [filename, setFilename] = useState();

  const handleFileChange = (value, filename, event) => {
    console.log("File uploaded: ", filename, value);
    setValue(value);
    setFilename(filename);
  };

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalImageSrc, setModalImageSrc] = useState("");
  const onImageClick = (imageSrc) => {
    setModalImageSrc(imageSrc);
    setIsModalOpen(true);
  };

  useEffect(() => {
    document?.getElementById("step")?.scrollIntoView();
  });

  return (
    <>
      <Modal
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Stack hasGutter id="step">
        <StackItem>
          <Title headingLevel="h1">Step 5: SAML Signing Certificate</Title>
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            <br />
            Download the certificate (Base64) from Step 3 and upload it below.
          </Text>
        </StackItem>
        <StackItem>
          <img
            src={azureStep13Image}
            alt="Step 5.1"
            className="step-image"
            onClick={() => onImageClick(azureStep13Image)}
          />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            <br />
          </Text>
        </StackItem>
        <StackItem>
          <Card className="card-shadow">
            <CardBody>
              <Form>
                <FormGroup label="Certificate (Base64)" fieldId="file-form">
                  <FileUpload
                    id="simple-file"
                    value={value}
                    filename={filename}
                    filenamePlaceholder="Drag or choose a file .cer, .cert, .key, .pem to upload."
                    browseButtonText="Upload"
                    onChange={handleFileChange}
                    // onReadStarted={handleFileReadStarted}
                    // onReadFinished={handleFileReadFinished}
                  />
                </FormGroup>
              </Form>
            </CardBody>
          </Card>
        </StackItem>
      </Stack>
    </>
  );
}
