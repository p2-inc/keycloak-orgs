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
import { InstructionProps } from "../../InstructionComponent";
import Step from "../../Step";
import { oktaValidateUsernamePassword } from "@app/services/OktaValidation";
import { useSessionStorage } from "react-use";

interface Props {
  onChange: (value: boolean) => void;
}

export const OktaStepTwo: FC<Props> = (props) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [alertText, setAlertText] = useState("");
  const [alertVariant, setAlertVariant] = useState("default");
  const [oktaUserInfo, setOktaUserInfo] = useSessionStorage("okta_user_info", {
    username: "",
    pass: "",
  });
  const [oktaCustomerIdentifier] = useSessionStorage(
    "okta_customer_identifier"
  );

  const onUsernameChange = (value: string) => {
    setUsername(value);
    setOktaUserInfo({ username: value, pass: oktaUserInfo.pass });
    props.onChange(username.length > 0 && password.length > 0);
  };

  const onPasswordChange = (value: string) => {
    setPassword(value);
    setOktaUserInfo({ username: oktaUserInfo.username, pass: value });
    props.onChange(username.length > 0 && password.length > 0);
  };

  const validateStep = async () => {
    const { username, pass } = oktaUserInfo;
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
              // <div>{alertText}</div>
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
                  value={oktaUserInfo.username}
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
                  value={oktaUserInfo.pass}
                  onChange={onPasswordChange}
                />
              </FormGroup>
              <Button onClick={validateStep}>Validate Input</Button>
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
