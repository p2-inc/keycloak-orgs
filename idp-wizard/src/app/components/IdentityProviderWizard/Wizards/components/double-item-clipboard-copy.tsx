import {
  Flex,
  FlexItem,
  ClipboardCopy,
  Card,
  CardBody,
  Form,
  FormGroup,
} from "@patternfly/react-core";
import { ArrowRightIcon } from "@patternfly/react-icons";
import cs from "classnames";
import React from "react";

interface Props {
  leftValue: string;
  leftLabel?: string;
  leftHelperText?: string;
  rightValue: string;
  rightLabel?: string;
  rightHelperText?: string;
  classes?: string;
}

export const DoubleItemClipboardCopy: React.FC<Props> = ({
  leftValue,
  leftLabel = "",
  leftHelperText = " ",
  rightValue,
  rightLabel = "",
  rightHelperText = " ",
  classes,
}) => {
  return (
    <Card className={cs("pf-u-box-shadow-sm pf-u-mb-md", classes)} isCompact>
      <CardBody>
        <Flex style={{ padding: "5px", flexWrap: "nowrap" }}>
          <FlexItem style={{ flex: 1 }}>
            <Form>
              <FormGroup
                label={leftLabel}
                fieldId="copy-form"
                helperText={leftHelperText}
              >
                <ClipboardCopy
                  isReadOnly
                  hoverTip="Copy"
                  clickTip="Copied"
                  className="clipboard-copy"
                  style={{ fontSize: "8px" }}
                >
                  {leftValue}
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </FlexItem>
          <FlexItem alignSelf={{ default: "alignSelfCenter" }}>
            <ArrowRightIcon />
          </FlexItem>
          <FlexItem style={{ flex: 1 }}>
            <Form>
              <FormGroup
                label={rightLabel}
                fieldId="copy-form"
                helperText={rightHelperText}
              >
                <ClipboardCopy
                  isReadOnly
                  hoverTip="Copy"
                  clickTip="Copied"
                  style={{ fontSize: "8px" }}
                >
                  {rightValue}
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </FlexItem>
        </Flex>
      </CardBody>
    </Card>
  );
};
