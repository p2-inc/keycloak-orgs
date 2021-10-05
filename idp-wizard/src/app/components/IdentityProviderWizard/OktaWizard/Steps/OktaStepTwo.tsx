import React, { FC, useState } from "react";
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

interface Props {
  onChange: (value: boolean) => void;
}

export const OktaStepTwo: FC<Props> = (props) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const onUsernameChange = (value: string) => {
    setUsername(value);
    props.onChange(username.length > 0 && password.length > 0);
  };

  const onPasswordChange = (value: string) => {
    setPassword(value);
    props.onChange(username.length > 0 && password.length > 0);
  };
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
        <Card className="card-shadow">
          <CardBody>
            <Form>
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
                  value={username}
                  onChange={onUsernameChange}
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
                  type="password"
                  id="simple-form-name-02"
                  name="simple-form-name-02"
                  value={password}
                  onChange={onPasswordChange}
                />
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      </StackItem>
    </Stack>
  );
};
