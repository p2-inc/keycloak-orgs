import React, { ReactElement } from "react";
import {
  Card,
  CardBody,
  ClipboardCopy,
  Form,
  FormGroup,
} from "@patternfly/react-core";
import cs from "classnames";

interface ClipboardCopyProps {
  label: string | ReactElement;
  initialValue: string;
  helperText?: string;
  classes?: string;
}
export function ClipboardCopyComponent({
  label,
  initialValue,
  helperText = "",
  classes = "",
}: ClipboardCopyProps) {
  return (
    <Card className={cs("pf-u-box-shadow-sm", classes)}>
      <CardBody>
        <Form>
          <FormGroup label={label} fieldId="copy-form" helperText={helperText}>
            <ClipboardCopy isReadOnly hoverTip="Copy" clickTip="Copied">
              {initialValue}
            </ClipboardCopy>
          </FormGroup>
        </Form>
      </CardBody>
    </Card>
  );
}
