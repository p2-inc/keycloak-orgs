import { Flex, Stack, StackItem, Title } from "@patternfly/react-core";
import React from "react";
import { InstructionComponent, InstructionProps } from ".";

interface StepProps {
  title: String;
  instructionList: InstructionProps[];
}
export function Step(props: StepProps) {
  return (
    <Stack hasGutter id="step">
      <StackItem>
        <Title headingLevel="h1">{props.title}</Title>
      </StackItem>
      <Flex direction={{ default: "column" }}>
        {props.instructionList.map((item, i) => {
          return (
            <InstructionComponent
              key={i}
              text={item.text}
              component={item.component}
            />
          );
        })}
      </Flex>
    </Stack>
  );
}
