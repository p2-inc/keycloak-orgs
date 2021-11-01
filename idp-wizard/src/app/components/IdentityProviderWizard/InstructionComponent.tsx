import {
  Flex,
  FlexItem,
  StackItem,
  Text,
  TextVariants,
} from "@patternfly/react-core";
import React from "react";

export interface InstructionProps {
  text?: string;
  component: JSX.Element;
}
function InstructionComponent(props: InstructionProps) {
  return (
    <Flex>
      {props.text && (
        <FlexItem className="step-instruction">
          <Text component={TextVariants.h2}>{props.text}</Text>
        </FlexItem>
      )}

      <FlexItem
        className={
          props.text ? "step-instruction-image" : "step-no-instruction"
        }
      >
        {props.component}
      </FlexItem>
    </Flex>
  );
}

export default InstructionComponent;
