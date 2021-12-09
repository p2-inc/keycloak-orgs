import { Button, Stack, StackItem, Title } from "@patternfly/react-core";
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
  error: boolean;
  isValidating: boolean;
  validationFunction: () => void;
}
export const WizardConfirmation: FC<SuccessProps> = ({
  title,
  message,
  buttonText,
  resultsText,
  error,
  isValidating,
  validationFunction,
}) => {
  return (
    <div className="container" style={{ border: 0 }}>
      <Stack hasGutter>
        <StackItem>
          {resultsText && resultsText !== "Final Validation Running..." ? (
            error ? (
              <ExclamationCircleIcon size="xl" color="red" />
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
          <Title headingLevel="h3">{resultsText}</Title>
        </StackItem>
        <StackItem>
          <Button isLoading={isValidating} onClick={validationFunction}>
            {buttonText}
          </Button>
        </StackItem>
      </Stack>
    </div>
  );
};
