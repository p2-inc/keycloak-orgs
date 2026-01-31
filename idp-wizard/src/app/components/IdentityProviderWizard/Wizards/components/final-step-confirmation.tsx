import { useAppSelector } from "@app/hooks/hooks";
import {
  Alert,
  Button,
  ClipboardCopy,
  Stack,
  StackItem,
  Text,
  Title,
} from "@patternfly/react-core";
import {
  CheckCircleIcon,
  ExclamationCircleIcon,
} from "@patternfly/react-icons";
import React, { FC } from "react";

interface SuccessProps {
  title: string;
  message: string;
  buttonText: string;
  resultsText: string;
  error: boolean | null;
  isValidating: boolean;
  disableButton: boolean;
  validationFunction: () => void;
  adminLink?: string;
  adminButtonText?: string;
  idpTestLink?: string;
}

// States should be
// On component => no submission yet. Tell user to finalize by creating instance in Keycloak
// On component, submitted with success => redirect to dashboard
// On component, submitted with error => show an error occurred and to review configuration

export const WizardConfirmation: FC<SuccessProps> = ({
  title,
  message,
  buttonText,
  resultsText,
  error,
  isValidating,
  validationFunction,
  disableButton = false,
  adminLink,
  adminButtonText,
  idpTestLink,
}) => {
  const apiMode = useAppSelector((state) => state.settings.apiMode);
  const isCloud = apiMode === "cloud";

  return (
    <div className="container">
      <Stack hasGutter>
        <StackItem>
          {error === true && <ExclamationCircleIcon size="xl" color="red" />}
          {error === false && <CheckCircleIcon size="xl" color="green" />}
          <Title headingLevel="h1" style={{ paddingLeft: 0 }}>
            {title}
          </Title>
        </StackItem>
        <StackItem>
          <Title headingLevel="h2">
            Finish by creating the identity provider.{" "}
            <u>The wizard is not complete until you do so.</u> After creating
            the identity provider, your users will be able to sign-in with{" "}
            <u>{message}</u>.
          </Title>
        </StackItem>
        <StackItem>
          <Title headingLevel="h3" style={{ color: error ? "red" : "inherit" }}>
            {resultsText}
          </Title>
        </StackItem>
        <StackItem>
          <Button
            isLoading={isValidating}
            onClick={validationFunction}
            isDisabled={disableButton}
          >
            {isCloud ? "Create Identity Provider" : buttonText}
          </Button>
        </StackItem>
        {idpTestLink && (
          <StackItem>
            <Alert
              variant="warning"
              title="Testing Single Sign-On"
              style={{ textAlign: "left" }}
            >
              <Text style={{ marginBottom: ".8rem" }}>
                Test signing in with SSO configuration to verify that the single
                sign-on connection was configured correctly. Copy the link below
                and open in another browser or an incognito window to avoid
                being logged out of the wizard.
              </Text>
              <ClipboardCopy
                hoverTip="Copy and open in another browser or incognito window, or you will be logged out of the wizard."
                clickTip="Copied. Open in another browser or incognito window, or you will be logged out of the wizard."
                className="clipboard-copy"
                style={{ fontSize: "8px" }}
              >
                {idpTestLink}
              </ClipboardCopy>
            </Alert>
          </StackItem>
        )}
        {!isCloud && disableButton && adminLink && (
          <StackItem>
            <Button component="a" href={adminLink}>
              {adminButtonText}
            </Button>
          </StackItem>
        )}
      </Stack>
    </div>
  );
};
