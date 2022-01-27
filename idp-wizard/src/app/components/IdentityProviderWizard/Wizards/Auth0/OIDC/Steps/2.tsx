import {
  Alert,
  Button,
  Card,
  CardBody,
  Form,
  FormGroup,
  TextInput,
} from "@patternfly/react-core";
import React, { useState, FC } from "react";
import Auth0Step3Image from "@app/images/auth0/OIDC/auth0-3.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

interface Props {
  onFormSubmission: ({
    domain,
    clientId,
    clientSecret,
  }: {
    domain: string;
    clientId: string;
    clientSecret: string;
  }) => API_RETURN_PROMISE;
  values: {
    domain: string;
    clientId: string;
    clientSecret: string;
  };
}

export const Auth0StepTwo: FC<Props> = ({ onFormSubmission, values }) => {
  const [alertText, setAlertText] = useState("");
  const [alertVariant, setAlertVariant] = useState("default");
  const [isValidating, setIsValidating] = useState(false);

  const [domain, setDomain] = useState(values.domain || "");
  const [clientId, setClientID] = useState(values.clientId || "");
  const [clientSecret, setClientSecret] = useState(values.clientSecret || "");

  const validateStep = async () => {
    setIsValidating(true);

    const resp = await onFormSubmission({ domain, clientId, clientSecret });

    setAlertVariant(resp.status);
    setAlertText(resp.message);

    setIsValidating(false);
  };

  const instructions: InstructionProps[] = [
    {
      component: <StepImage src={Auth0Step3Image} alt="Step 2.1" />,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            {alertText && (
              <Alert
                variant={alertVariant === "ERROR" ? "danger" : "success"}
                isInline
                title={alertText}
              />
            )}

            <Form>
              <FormGroup
                label="Domain"
                isRequired
                fieldId="domain"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="domain-text"
                  name="Domain"
                  aria-describedby="Domain"
                  value={domain}
                  onChange={setDomain}
                />
              </FormGroup>
              <FormGroup
                label="Client ID"
                isRequired
                fieldId="client-id"
                className="form-label"
              >
                <TextInput
                  isRequired
                  type="text"
                  id="client-id-text"
                  name="Client ID"
                  aria-describedby="Client ID"
                  value={clientId}
                  onChange={setClientID}
                />
              </FormGroup>
              <FormGroup
                label="Client Secret"
                isRequired
                fieldId="client-secret"
                className="form-label"
              >
                <TextInput
                  isRequired
                  id="client-secret-text"
                  name="Client Secret"
                  aria-describedby="Client Secret"
                  value={clientSecret}
                  onChange={setClientSecret}
                  type="password"
                />
              </FormGroup>
              <Button
                style={{ textAlign: "center" }}
                isLoading={isValidating}
                onClick={validateStep}
              >
                Validate Auth0 App Basic Settings
              </Button>
            </Form>
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Provide Domain And Credentials"
      instructionList={instructions}
    />
  );
};
