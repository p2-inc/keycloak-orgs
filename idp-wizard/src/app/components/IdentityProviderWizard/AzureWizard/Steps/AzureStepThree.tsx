import {
  Title,
  Stack,
  StackItem,
  Text,
  TextVariants,
} from "@patternfly/react-core";
import React from "react";
import azureStep7Image from "@app/images/azure/azure-7.png";
import azureStep8Image from "@app/images/azure/azure-8.png";

export function AzureStepThree() {
  return (
    <Stack hasGutter>
      <StackItem>
        <Title headingLevel="h1">Step 3: User Attributes & Claims</Title>
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          Click the Edit icon in the top right of the second step.
        </Text>
      </StackItem>
      <StackItem>
        <img src={azureStep7Image} alt="Step 2.1" />
      </StackItem>
      <StackItem>
        <Text component={TextVariants.h2}>
          <br />
          Fill in the following Attribute Statements and select "Next".
        </Text>
      </StackItem>
      <StackItem>
        <img src={azureStep8Image} alt="Step 2.1" className="step-image" />
      </StackItem>
    </Stack>
  );
}
