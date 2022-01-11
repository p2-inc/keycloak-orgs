import React, { FC, useState } from "react";
import {
  Form,
  FormGroup,
  TextInput,
  Card,
  CardBody,
  Button,
  Alert,
} from "@patternfly/react-core";
import { InstructionProps, Step } from "@wizardComponents";
import { oktaValidateUsernamePassword } from "@app/services/OktaValidation";

interface Props {
  onChange: (value: boolean) => void;
}

export const OktaStepTwo: FC<Props> = (props) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isValidating, setIsValidating] = useState(false);
  const [alertText, setAlertText] = useState("");
  const [alertVariant, setAlertVariant] = useState("default");

  const oktaCustomerIdentifier =
    sessionStorage.getItem("okta_customer_identifier") ||
    process.env.OKTA_DEFAULT_CUSTOMER_IDENTIFER;

  const onUsernameChange = (value: string) => {
    setUsername(value);
    sessionStorage.setItem("okta_un", value);
    //setOktaUserInfo({ username: value, pass: oktaUserInfo.pass });
    props.onChange(username.length > 0 && password.length > 0);
  };

  const onPasswordChange = (value: string) => {
    setPassword(value);
    sessionStorage.setItem("okta_p", value);
    // setOktaUserInfo({ username: oktaUserInfo.username, pass: value });
    props.onChange(username.length > 0 && password.length > 0);
  };

  const validateStep = async () => {
    setIsValidating(true);
    const username = sessionStorage.getItem("okta_un") || "";
    const pass = sessionStorage.getItem("okta_p") || "";
    await oktaValidateUsernamePassword(
      `${oktaCustomerIdentifier}.ldap.okta.com`,
      username,
      pass,
      `dc=${oktaCustomerIdentifier}, dc=okta, dc=com`
    )
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
      text: "Enter your LDAP administrator credentials",
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
                label="Okta Administrator Password"
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
    <Step
      title="Step 2: LDAP Authentication"
      instructionList={instructionList}
    />
  );
};
