import React, { ReactElement } from "react";
import { Flex, FlexItem, Text, TextVariants } from "@patternfly/react-core";
import cs from "classnames";

export interface InstructionProps {
  text?: string | ReactElement;
  component: JSX.Element | ReactElement[];
}
export function InstructionComponent({ text, component }: InstructionProps) {
  return (
    <div
      className={cs("p2-step-instruction", {
        columns: text && component,
      })}
    >
      {text && (
        <div className="pf-u-mb-md" style={{ maxWidth: 960 }}>
          <Text component={TextVariants.h2}>{text}</Text>
        </div>
      )}

      <div
        className={cs({
          "step-instruction-image": text,
          "step-no-instruction": !text,
        })}
      >
        {component}
      </div>
    </div>
  );
}
