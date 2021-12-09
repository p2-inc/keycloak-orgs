import React, { FC, useState } from "react";
import oktaStep1Image from "@app/images/okta/okta-1.png";
import oktaStep2Image from "@app/images/okta/okta-2.png";
import {
  Alert,
  Form,
  FormGroup,
  TextInput,
  Card,
  CardBody,
  Modal,
  ModalVariant,
  Button,
} from "@patternfly/react-core";
import Step from "../../../Step";
import { InstructionProps } from "../../../InstructionComponent";
import { useImageModal } from "@app/hooks/useImageModal";
import { oktaStepOneValidation } from "@app/services/OktaValidation";
import { useSessionStorage } from "react-use";

interface Props {
  onChange: (value: boolean) => void;
}

export interface IOktaValues {
  customerIdentifier: string;
  ldapValue: string;
  ldapBaseND: string;
  ldapUserBaseDN: string;
  ldapGroupBaseND: string;
}

export const OktaStepOne: FC<Props> = (props) => {
  const [isModalOpen, modalImageSrc, { onImageClick }, setIsModalOpen] =
    useImageModal();
  const [alertText, setAlertText] = useState("");
  const [alertVariant, setAlertVariant] = useState("default");
  const [isValidating, setIsValidating] = useState(false);
  const oktaCustomerIdentifier =
    sessionStorage.getItem("okta_customer_identifier") ||
    process.env.OKTA_DEFAULT_CUSTOMER_IDENTIFER;

  const customerIdentifier = oktaCustomerIdentifier;

  const [ldapValue, setLDAPValue] = useState(
    customerIdentifier + ".ldap.okta.com"
  );
  const [ldapBaseDN, setldapBaseDN] = useState(
    `dc=${customerIdentifier}, dc=okta, dc=com`
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
    sessionStorage.setItem("okta_customer_identifier", custIdentifer);
  };

  const validateStep = async () => {
    setIsValidating(true);
    await oktaStepOneValidation(`${ldapValue}:636`)
      .then((res) => {
        setAlertText(res.message);
        setAlertVariant(res.status);
        props.onChange(true);
      })
      .catch(() => {
        setAlertText("Error, could not validate okta");
        setAlertVariant("danger");
      });

    setIsValidating(false);
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
            {alertText && (
              <Alert
                variant={alertVariant == "error" ? "danger" : "success"}
                isInline
                title={alertText}
              />
            )}

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
              <Button
                style={{ width: "200px" }}
                isLoading={isValidating}
                onClick={validateStep}
              >
                Validate Input
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
        isOpen={isModalOpen || false}
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
