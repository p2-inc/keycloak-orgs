import {
  Form,
  Card,
  CardBody,
  FormGroup,
  ClipboardCopy,
  Flex,
  FlexItem,
} from "@patternfly/react-core";
import React from "react";
import azureStep7Image from "@app/images/azure/azure-7.png";
import azureStep8Image from "@app/images/azure/azure-8.png";
import { ArrowRightIcon } from "@patternfly/react-icons";
import { InstructionProps, Step, StepImage } from "@wizardComponents";

interface IClaims {
  name: string;
  value: string;
}

export function AzureStepThree() {
  const claimNames = [
    {
      name: "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
      value: "user.mail",
    },
    {
      name: "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
      value: "user.givenname",
    },
    {
      name: "http://schemas.microsoft.com/identity/claims/name",
      value: "user.userprincipalname",
    },
    {
      name: "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
      value: "user.surname",
    },
  ];

  const renderClipboardCopyHeader = () => {
    return (
      <Flex style={{ padding: "5px" }}>
        <FlexItem
          style={{ width: "550px", fontSize: "16px", fontWeight: "bold" }}
        >
          Claim Name
        </FlexItem>
        <FlexItem>
          <ArrowRightIcon />
        </FlexItem>
        <FlexItem
          style={{ width: "250px", fontSize: "16px", fontWeight: "bold" }}
        >
          Value
        </FlexItem>
      </Flex>
    );
  };

  const renderClipboardCopy = (vals: IClaims) => {
    return (
      <Flex style={{ padding: "5px" }}>
        <FlexItem>
          <ClipboardCopy
            isReadOnly
            hoverTip="Copy"
            clickTip="Copied"
            className="clipboard-copy"
            style={{ width: "550px", fontSize: "8px" }}
          >
            {vals.name}
          </ClipboardCopy>
        </FlexItem>
        <FlexItem>
          <ArrowRightIcon />
        </FlexItem>
        <FlexItem>
          <ClipboardCopy
            isReadOnly
            hoverTip="Copy"
            clickTip="Copied"
            style={{ width: "250px", fontSize: "8px" }}
          >
            {vals.value}
          </ClipboardCopy>
        </FlexItem>
      </Flex>
    );
  };

  const instructionList: InstructionProps[] = [
    {
      text: "Click the Edit icon in the top right of the second step.",
      component: <StepImage src={azureStep7Image} alt="Step 2.1" />,
    },
    {
      component: (
        <Card style={{ width: "1000px" }} className="card-shadow">
          <CardBody>
            <Form>
              <FormGroup fieldId="copy-form">
                {renderClipboardCopyHeader()}
                {claimNames.map((claim) => {
                  return renderClipboardCopy(claim);
                })}
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      ),
    },
    {
      text: 'Fill in the following Attribute Statements and select "Next".',
      component: <StepImage src={azureStep8Image} alt="Step 2.1" />,
    },
  ];

  return (
    <Step
      title="Step 3: User Attributes & Claims"
      instructionList={instructionList}
    />
  );
}
