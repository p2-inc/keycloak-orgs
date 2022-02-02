import React, { FC, useState } from "react";
import {
  ActionGroup,
  Alert,
  Button,
  Card,
  CardBody,
  Form,
  FormGroup,
  TextInput,
} from "@patternfly/react-core";
import { InstructionProps, Step } from "@wizardComponents";
import { StepImage } from "../../../../components/zoom-image";
import { API_STATUS } from "@app/configurations/api-status";
import * as Images from "@app/images/azure/saml";

interface Props {
  validateMetadata: ({ metadataUrl }: { metadataUrl: string }) => Promise<{
    status: API_STATUS;
    message: string;
  }>;
}

export const AzureStepThree: FC<Props> = ({ validateMetadata }) => {
  const [metadataUrl, setMetadataUrl] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<null | {
    status: API_STATUS;
    message: string;
  }>(null);

  const submitMetadata = async () => {
    setIsLoading(true);
    const validateResult = await validateMetadata({ metadataUrl });
    setResult(validateResult);
    setIsLoading(false);
  };

  const instructions: InstructionProps[] = [
    {
      text: "Copy the App Federation Metadata URL.",
      component: <StepImage src={Images.AzureSaml1a} alt="Step 1a" />,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            {result && (
              <Alert
                variant={
                  result.status === API_STATUS.ERROR ? "danger" : "default"
                }
                title={result.message}
                aria-live="polite"
                isInline
              />
            )}

            <Form>
              <FormGroup label="Metadata URL" isRequired fieldId="metadata-url">
                <TextInput
                  isRequired
                  type="text"
                  id="metadata-text"
                  name="Metadata URL"
                  aria-describedby="App Federation Metadata URL"
                  value={metadataUrl}
                  onChange={setMetadataUrl}
                />
              </FormGroup>
              <ActionGroup>
                <Button
                  variant="primary"
                  isLoading={isLoading}
                  onClick={submitMetadata}
                >
                  Validate SAML Config
                </Button>
              </ActionGroup>
            </Form>
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 3: Validate Azure SAML Metadata file"
      instructionList={instructions}
    />
  );
};
