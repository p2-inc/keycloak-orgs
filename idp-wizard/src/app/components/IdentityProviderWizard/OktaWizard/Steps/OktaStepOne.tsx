import React, { FC, useState } from "react";
// import { PageSection, Title, Image } from '@patternfly/react-core';
import oktaStep1Image from "@app/images/okta/okta-1.png";
import oktaStep2Image from "@app/images/okta/okta-2.png";
import {
  Form,
  FormGroup,
  TextInput,
  Card,
  CardBody,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import Step from "../../Step";
import { InstructionProps } from "../../InstructionComponent";

export const OktaStepOne: FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalImageSrc, setModalImageSrc] = useState("");
  const onImageClick = (imageSrc) => {
    setModalImageSrc(imageSrc);
    setIsModalOpen(true);
  };

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

  const instructionList: InstructionProps[] = [
    {
      text: "Directory Integrations",
      component: (
        <img
          src={oktaStep1Image}
          alt="Step 1.1"
          className="step-image"
          onClick={() => onImageClick(oktaStep1Image)}
        />
      ),
    },
    {
      text: "If you have not already, add LDAP Interface from the Okta Directory Integration section.",
      component: (
        <img
          src={oktaStep2Image}
          alt="Step 1.2"
          className="step-image"
          onClick={() => onImageClick(oktaStep2Image)}
        />
      ),
    },
    {
      text: "Note the Settings and input them below.",
      component: <></>,
    },
    {
      component: (
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
      ),
    },
  ];

  return (
    <>
      <Modal
        variant={ModalVariant.large}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      >
        <img src={modalImageSrc} alt="Step Image" />
      </Modal>
      <Step
        title="Step 1: Enable LDAP Interface"
        instructionList={instructionList}
      />
    </>
  );
};
