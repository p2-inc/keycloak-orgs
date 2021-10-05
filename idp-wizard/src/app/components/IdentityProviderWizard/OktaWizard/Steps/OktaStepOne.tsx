import React, { FC, useState } from "react";
// import { PageSection, Title, Image } from '@patternfly/react-core';
import oktaStep1Image from "@app/images/okta/okta-1.png";
import oktaStep2Image from "@app/images/okta/okta-2.png";
import {
  Form,
  FormGroup,
  TextInput,
  Stack,
  StackItem,
  Title,
  Text,
  TextVariants,
  Card,
  CardBody,
} from "@patternfly/react-core";
import { css } from "@patternfly/react-styles";

export const OktaStepOne: FC = () => {
  const customerIdentifier = "dev-32234-234";

  const [ldapValue, setLDAPValue] = useState(
    customerIdentifier + ".ldap.okta.com"
  );
  const [ldapBaseDN, setldapBaseDN] = useState(
    `dc=${customerIdentifier}, dc=okta, dc-com`
  );
  const [ldapUserBaseDN, setldapUserBaseDN] = useState(
    `ou=users, dc=${customerIdentifier}, dc=okta, dc=com`
  );
  const [ldapGroupBaseDN, setldapGroupBaseDN] = useState(
    `ou=groups, dc=${customerIdentifier}, dc=okta, dc=com`
  );

  const handleLDAPValueChange = (value: string) => {
    setLDAPValue(value);
    const custIdentifer = value.split(".")[0];
    setldapBaseDN(`dc=${custIdentifer}, dc=okta, dc-com`);
    setldapUserBaseDN(`ou=users, dc=${custIdentifer}, dc=okta, dc=com`);
    setldapGroupBaseDN(`ou=groups, dc=${custIdentifer}, dc=okta, dc=com`);
  };

  return (
    <Stack hasGutter>
      <StackItem>
        <Title headingLevel="h1">Step 1: Enable LDAP Interface</Title>
      </StackItem>
      <StackItem>
        <img src={oktaStep1Image} alt="Step 1.1" className="step-image" />
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          If you have not already, add LDAP Interface from the Okta Directory //
          Integration section.
        </Text>
      </StackItem>
      <StackItem>
        <img src={oktaStep2Image} alt="Step 1.2" className="step-image" />
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          Note the Settings and input them below.
        </Text>
      </StackItem>
      <StackItem>
        <Card className="card-shadow">
          <CardBody>
            <Form>
              <FormGroup
                label="1. LDAP Host"
                isRequired
                fieldId="simple-form-name-01"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-01"
                  name="simple-form-name-01"
                  aria-describedby="simple-form-name-01-helper"
                  value={ldapValue}
                  onChange={handleLDAPValueChange}
                />
              </FormGroup>
              <FormGroup
                label="2. LDAP SSL Port"
                isRequired
                fieldId="simple-form-name-02"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-02"
                  name="simple-form-name-02"
                  aria-describedby="simple-form-name-02-helper"
                  value="636"
                />
              </FormGroup>
              <FormGroup
                label="3. LDAP Base DN"
                isRequired
                fieldId="simple-form-name-03"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-03"
                  name="simple-form-name-03"
                  aria-describedby="simple-form-name-03-helper"
                  value={ldapBaseDN}
                />
              </FormGroup>
              <FormGroup
                label="4. LDAP User Base DN"
                isRequired
                fieldId="simple-form-name-04"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-04"
                  name="simple-form-name-04"
                  aria-describedby="simple-form-name-04-helper"
                  value={ldapUserBaseDN}
                />
              </FormGroup>
              <FormGroup
                label="5. LDAP Group Base DN"
                isRequired
                fieldId="simple-form-name-05"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="simple-form-name-05"
                  name="simple-form-name-05"
                  aria-describedby="simple-form-name-05-helper"
                  value={ldapGroupBaseDN}
                />
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      </StackItem>
    </Stack>
  );
};
