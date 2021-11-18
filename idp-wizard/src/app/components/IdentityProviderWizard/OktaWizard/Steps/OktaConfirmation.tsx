import { oktaCreateFederationAndSyncUsers } from "@app/services/OktaValidation";
import { Button, Stack, StackItem, Title } from "@patternfly/react-core";
import { CheckCircleIcon, CrossIcon } from "@patternfly/react-icons";
import { string } from "prop-types";
import React, { FC, useState } from "react";
import { useSessionStorage } from "react-use";

interface SuccessProps {
  title: string;
  message: string;
  buttonText: string;
}
export const WizardConfirmation: FC<SuccessProps> = ({
  title,
  message,
  buttonText,
}) => {
  const [oktaCustomerIdentifier] = useSessionStorage(
    "okta_customer_identifier",
    ""
  );
  const [oktaUserInfo] = useSessionStorage("okta_user_info", {
    username: string,
    pass: string,
  });
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);

  const validateWizard = async () => {
    setResults("Final Validation Running...");
    const results = await oktaCreateFederationAndSyncUsers(
      oktaCustomerIdentifier,
      oktaUserInfo.username!,
      oktaUserInfo.pass!
    );

    setError(results.status == "error");
    setResults("Results: " + JSON.stringify(results));
  };
  return (
    <div className="container" style={{ border: 0 }}>
      <Stack hasGutter>
        <StackItem>
          {results !== "Final Validation Running..." ? (
            error ? (
              <CrossIcon size="xl" color="red" />
            ) : (
              <CheckCircleIcon size="xl" color="green" />
            )
          ) : (
            ""
          )}
        </StackItem>
        <StackItem>
          <Title headingLevel="h1">{title}</Title>
        </StackItem>
        <StackItem>
          <Title headingLevel="h2">{message}</Title>
        </StackItem>
        <StackItem>
          <Title headingLevel="h2">{results}</Title>
        </StackItem>
        <StackItem>
          <Button onClick={validateWizard}>{buttonText}</Button>
        </StackItem>
      </Stack>
    </div>
  );
};
