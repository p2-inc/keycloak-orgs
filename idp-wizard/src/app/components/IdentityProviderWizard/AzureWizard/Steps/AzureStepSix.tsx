import {
  Card,
  CardBody,
  Title,
  Form,
  FormGroup,
  Stack,
  StackItem,
  Text,
  TextInput,
  TextVariants,
} from "@patternfly/react-core";
import React from "react";
import azureStep14Image from "@app/images/azure/azure-14.png";

export function AzureStepSix() {
  return (
    <Stack hasGutter>
      <StackItem>
        <Title headingLevel="h1">Step 6: Provide a Login URL</Title>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          Copy the Login URL from Step 4 and enter it below.
        </Text>
      </StackItem>
      <StackItem>
        <img src={azureStep14Image} alt="Step 6.1" className="step-image" />
      </StackItem>
      <StackItem>
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
      </StackItem>
    </Stack>
  );
}
