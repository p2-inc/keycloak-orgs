import React, { ReactElement } from "react";
import { Flex, FlexItem, Text, TextVariants } from "@patternfly/react-core";
import cs from "classnames";

export interface InstructionProps {
  text?: string | ReactElement;
  component: JSX.Element;
}
export function InstructionComponent({ text, component }: InstructionProps) {
  return (
    <Flex>
      {text && (
        <FlexItem className="step-instruction">
          <Text component={TextVariants.h2}>{text}</Text>
        </FlexItem>
      )}

      <FlexItem
        className={cs({
          "step-instruction-image": text,
          "step-no-instruction": !text,
        })}
      >
        {component}
      </FlexItem>
    </Flex>
  );
}
