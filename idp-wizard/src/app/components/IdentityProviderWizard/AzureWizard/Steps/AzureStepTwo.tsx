import {
  Card,
  CardBody,
  ClipboardCopy,
  Form,
  FormGroup,
  Stack,
  StackItem,
  Text,
  TextVariants,
  Title,
} from "@patternfly/react-core";
import React from "react";
import azureStep5Image from "@app/images/azure/azure-5.png";
import azureStep6Image from "@app/images/azure/azure-6.png";

export function AzureStepTwo() {
  return (
    <Stack hasGutter>
      <StackItem>
        <Title headingLevel="h1">Step 2: Basic SAML Configuration</Title>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          Click the Edit icon in the top right of the first step.
        </Text>
      </StackItem>
      <StackItem>
        <img src={azureStep5Image} alt="Step 2.1" className="step-image" />
      </StackItem>
      <StackItem>
        <Card>
          <CardBody>
            <Form>
              <FormGroup label="Copy this identifier" fieldId="copy-form">
                <ClipboardCopy isReadOnly hoverTip="Copy" clickTip="Copied">
                  https://auth.phasetwo.io/30945803490g90rg493040
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      </StackItem>
      <StackItem>
        <Card>
          <CardBody>
            <Form>
              <FormGroup label="Copy this Reply URL" fieldId="copy-form">
                <ClipboardCopy isReadOnly hoverTip="Copy" clickTip="Copied">
                  https://auth.phasetwo.io/sso/saml/acs/30945803490g90rg493040
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      </StackItem>
      <StackItem>
        <br />
        <Text component={TextVariants.h2}>
          Submit the identifier and the Reply URL in the Basic SAML
          Configuration.
        </Text>
      </StackItem>
      <StackItem>
        <img src={azureStep6Image} alt="Step 2.2" className="step-image" />
      </StackItem>
    </Stack>
  );
}
