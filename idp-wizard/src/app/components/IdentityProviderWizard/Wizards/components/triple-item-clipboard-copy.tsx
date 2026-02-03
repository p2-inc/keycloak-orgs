import {
  Flex,
  FlexItem,
  ClipboardCopy,
  Card,
  CardBody,
  Form,
  FormGroup,
} from "@patternfly/react-core";
import cs from "classnames";
import React from "react";

interface Props {
  firstValue: string;
  firstLabel?: string;
  firstHelperText?: string;
  secondValue: string;
  secondLabel?: string;
  secondHelperText?: string;
  thirdValue: string;
  thirdLabel?: string;
  thirdHelperText?: string;
  classes?: string;
}

export const TripleItemClipboardCopy: React.FC<Props> = ({
  firstValue,
  firstLabel = "",
  firstHelperText = " ",
  secondValue,
  secondLabel = "",
  secondHelperText = " ",
  thirdValue,
  thirdLabel = "",
  thirdHelperText = " ",
  classes,
}) => {
  return (
    <Card className={cs("pf-u-box-shadow-sm pf-u-mb-md", classes)} isCompact>
      <CardBody>
        <Flex style={{ padding: "5px", flexWrap: "nowrap" }}>
          <FlexItem style={{ flex: 1 }}>
            <Form>
              <FormGroup
                label={firstLabel}
                fieldId="copy-form"
                helperText={firstHelperText}
              >
                <ClipboardCopy
                  isReadOnly
                  hoverTip="Copy"
                  clickTip="Copied"
                  className="clipboard-copy"
                  style={{ fontSize: "8px" }}
                >
                  {firstValue}
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </FlexItem>
          <FlexItem style={{ flex: 1 }}>
            <Form>
              <FormGroup
                label={secondLabel}
                fieldId="copy-form"
                helperText={secondHelperText}
              >
                <ClipboardCopy
                  isReadOnly
                  hoverTip="Copy"
                  clickTip="Copied"
                  style={{ fontSize: "8px" }}
                >
                  {secondValue}
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </FlexItem>
          <FlexItem style={{ flex: 1 }}>
            <Form>
              <FormGroup
                label={thirdLabel}
                fieldId="copy-form"
                helperText={thirdHelperText}
              >
                <ClipboardCopy
                  isReadOnly
                  hoverTip="Copy"
                  clickTip="Copied"
                  style={{ fontSize: "8px" }}
                >
                  {thirdValue}
                </ClipboardCopy>
              </FormGroup>
            </Form>
          </FlexItem>
        </Flex>
      </CardBody>
    </Card>
  );
};
