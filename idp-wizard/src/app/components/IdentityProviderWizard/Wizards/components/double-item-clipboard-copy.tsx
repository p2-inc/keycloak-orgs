import {
  Flex,
  FlexItem,
  ClipboardCopy,
  Card,
  CardBody,
} from "@patternfly/react-core";
import { ArrowRightIcon } from "@patternfly/react-icons";
import cs from "classnames";
import React from "react";

interface Props {
  leftValue: string;
  rightValue: string;
  classes?: string;
}

export const DoubleItemClipboardCopy: React.FC<Props> = ({
  leftValue,
  rightValue,
  classes,
}) => {
  return (
    <Card className={cs("pf-u-box-shadow-sm", classes)} isCompact>
      <CardBody>
        <Flex style={{ padding: "5px", flexWrap: "nowrap" }}>
          <FlexItem style={{ flex: 1 }}>
            <ClipboardCopy
              isReadOnly
              hoverTip="Copy"
              clickTip="Copied"
              className="clipboard-copy"
              style={{ fontSize: "8px" }}
            >
              {leftValue}
            </ClipboardCopy>
          </FlexItem>
          <FlexItem>
            <ArrowRightIcon />
          </FlexItem>
          <FlexItem style={{ flex: 1 }}>
            <ClipboardCopy
              isReadOnly
              hoverTip="Copy"
              clickTip="Copied"
              style={{ fontSize: "8px" }}
            >
              {rightValue}
            </ClipboardCopy>
          </FlexItem>
        </Flex>
      </CardBody>
    </Card>
  );
};
