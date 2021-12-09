import React, { FC, useState } from "react";
import azureStep1aImage from "@app/images/azure/azure-1a.png";
import {
  Alert,
  Button,
  Card,
  CardBody,
  Form,
  FormGroup,
  Modal,
  ModalVariant,
  TextInput,
} from "@patternfly/react-core";
import { InstructionProps } from "../../../InstructionComponent";
import { Step } from "../../components";
import { useImageModal } from "@app/hooks/useImageModal";
import { azureStepOneAValidation } from "@app/services/AzureValidation";
import { useSessionStorage } from "react-use";

interface Props {
  onChange: (value: boolean) => void;
}

export const AzureStepOneA: FC<Props> = (props) => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();
  const [alertText, setAlertText] = useState("");
  const [alertVariant, setAlertVariant] = useState("default");
  const [isValidating, setIsValidating] = useState(false);
  const [metadataURL, setMetadataURL] = useSessionStorage("azure_metaurl", "");

  const handleMetadataURLValueChange = (value: string) => {
    setMetadataURL(value);
    //Store the data to session storage
  };

  const validateStep = async () => {
    setIsValidating(true);
    await azureStepOneAValidation(`${metadataURL}`, false)
      .then((res) => {
        setAlertText(res.message);
        setAlertVariant(res.status);
        props.onChange(true);
      })
      .catch(() => {
        setAlertText("Error, could not create IdP in Keycloak");
        setAlertVariant("danger");
      });
    setIsValidating(false);
  };

  const instructions: InstructionProps[] = [
    {
      text: "Copy the App Federation Metadata URL.",
      component: (
        <img
          src={azureStep1aImage}
          alt="Step 1a"
          className="step-image"
          onClick={() => onImageClick(azureStep1aImage)}
        />
      ),
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            {alertText && (
              <Alert
                variant={alertVariant == "error" ? "danger" : "success"}
                isInline
                title={alertText}
              />
            )}

            <Form>
              <FormGroup
                label="Metadata URL"
                isRequired
                fieldId="metadata-url"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="metadata-text"
                  name="Metadata URL"
                  aria-describedby="App Federation Metadata URL"
                  value={metadataURL}
                  onChange={handleMetadataURLValueChange}
                />
              </FormGroup>
              <Button
                style={{ width: "300px", textAlign: "center" }}
                isLoading={isValidating}
                onClick={validateStep}
              >
                Validate SAML Config
              </Button>
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
        title="Step 3: Validate Azure SAML Metadata file"
        instructionList={instructions}
      />
    </>
  );
};
