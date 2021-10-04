import React, { FC } from "react";
import {
  Form,
  FormGroup,
  TextInput,
  Text,
  TextVariants,
  Stack,
  StackItem,
  Title,
  Card,
  CardBody,
} from "@patternfly/react-core";

export const OktaStepTwo: FC = () => {
  return (
    <Stack hasGutter>
      <StackItem>
        <Title headingLevel="h1">Step 2: LDAP Authentication</Title>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          Enter your LDAP administrator credentials
        </Text>
      </StackItem>
      <StackItem>
        <Card>
          <CardBody>
            <Form className="form-container">
              <FormGroup
                label="Okta Administrator Username"
                isRequired
                fieldId="simple-form-name-01"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-01"
                  name="simple-form-name-01"
                />
              </FormGroup>
              <FormGroup
                label="2. Okta Administrator Password"
                isRequired
                fieldId="simple-form-name-02"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-02"
                  name="simple-form-name-02"
                />
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      </StackItem>
    </Stack>
  );
};
