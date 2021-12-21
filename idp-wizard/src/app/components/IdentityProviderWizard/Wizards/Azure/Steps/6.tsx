import {
  Card,
  CardBody,
  Form,
  FormGroup,
  TextInput,
} from "@patternfly/react-core";
import React from "react";
import azureStep14Image from "@app/images/azure/azure-14.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

export function AzureStepSix() {
  const instructionList: InstructionProps[] = [
    {
      text: "Copy the Login URL from Step 4 and enter it below.",
      component: <StepImage src={azureStep14Image} alt="Step 6.1" />,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            <Form>
              <FormGroup label="Login URL" fieldId="copy-form">
                <TextInput
                  name="Login URL"
                  id="loginURL"
                  aria-label="Login URL"
                  value={"https://app.phasetwo.io/realms/test/saml"}
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
      title="Step 6: Provide a Login URL"
      instructionList={instructionList}
    />
  );
}
