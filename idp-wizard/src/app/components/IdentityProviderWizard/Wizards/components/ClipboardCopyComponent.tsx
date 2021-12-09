import {
  Card,
  CardBody,
  ClipboardCopy,
  Form,
  FormGroup,
} from "@patternfly/react-core";
import React from "react";

interface ClipboardCopyProps {
  label: string;
  initialValue: string;
}
export function ClipboardCopyComponent(props: ClipboardCopyProps) {
  return (
    <Card className="card-shadow">
      <CardBody>
        <Form>
          <FormGroup label={props.label} fieldId="copy-form">
            <ClipboardCopy isReadOnly hoverTip="Copy" clickTip="Copied">
              {props.initialValue}
            </ClipboardCopy>
          </FormGroup>
        </Form>
      </CardBody>
    </Card>
  );
}
