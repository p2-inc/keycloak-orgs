import { Button, Stack, StackItem, Title } from "@patternfly/react-core";
import { AngryIcon, CheckCircleIcon } from "@patternfly/react-icons";
import React, { FC } from "react";
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
  return (
    <div className="container" style={{ border: 0 }}>
      <Stack hasGutter>
        <StackItem>
          <CheckCircleIcon size="xl" color="green" />
        </StackItem>
        <StackItem>
          <Title headingLevel="h1">{title}</Title>
        </StackItem>
        <StackItem>
          <Title headingLevel="h2">{message}</Title>
        </StackItem>
        <StackItem>
          <Button>{buttonText}</Button>
        </StackItem>
      </Stack>
    </div>
  );
};
