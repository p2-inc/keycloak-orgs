import React, { FC, useState } from "react";
import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Form,
  FormGroup,
  InputGroup,
  TextInput,
  Text,
  TextVariants,
  Stack,
  StackItem,
  Title,
} from "@patternfly/react-core";
import image from "@app/images/okta/okta-3.png";

export const OktaStepThree: FC = () => {
  const [groupList, setGroupList] = useState<string[]>(["Group 1", ""]);

  const handleInputChange = (value, index) => {
    const list = [...groupList];
    list[index] = value;
    if (index == list.length - 1 && list[index].length > 0) {
      list.push("");
    }

    setGroupList(list);
  };

  return (
    <>
      <Stack>
        <StackItem>
          <Title headingLevel="h1">Step 3: Group Mapping</Title>
        </StackItem>

        <StackItem>
          <Text component={TextVariants.h2} className="pf-u-font-weight-bold">
            This is an optional step. If you have groups defined in Okta, you
            will find them in the Directory {"->"} Groups section.
          </Text>
        </StackItem>
        <StackItem>
          <img src={image} alt="Step3" className="step-image" /> <br />
        </StackItem>
        <StackItem>
          <Text component={TextVariants.h2}>
            If you want to limit groups that can access the demo.phasetwo.io
            app, enter those groups below.
          </Text>
        </StackItem>
        <StackItem>
          <Card>
            <CardBody>
              <Form>
                <FormGroup label="Groups" fieldId="input-form">
                  {groupList.map((x, i) => {
                    console.log("rendering");
                    return (
                      <InputGroup style={{ padding: "2px" }}>
                        <TextInput
                          key={i}
                          value={x}
                          name={i.toString()}
                          id={i.toString()}
                          aria-label="Group"
                          onChange={(value) => handleInputChange(value, i)}
                        />
                      </InputGroup>
                    );
                  })}
                </FormGroup>
              </Form>
            </CardBody>
          </Card>
        </StackItem>
      </Stack>

      {/* <Card>
        <CardHeader>
          <Text component={TextVariants.h1}>Step 3: Group Mapping</Text>
        </CardHeader>
        <CardBody>
          <Text component={TextVariants.h2}>
            This is an optional step. If you have groups defined in Okta, you
            will find them in the Directory {"->"} Groups section.{" "}
          </Text>
          <img src={image} alt="Step3" /> <br />
          <Text component={TextVariants.h2}>
            If you want to limit groups that can access the demo.phasetwo.io
            app, enter those groups below.
          </Text>
          <Card>
            <CardBody>
              <Form>
                <FormGroup label="Groups" fieldId="input-form">
                  {groupList.map((x, i) => {
                    console.log("rendering");
                    return (
                      <InputGroup style={{ padding: "2px" }}>
                        <TextInput
                          key={i}
                          value={x}
                          name={i.toString()}
                          id={i.toString()}
                          aria-label="Group"
                          onChange={(value) => handleInputChange(value, i)}
                        />
                      </InputGroup>
                    );
                  })}
                </FormGroup>
              </Form>
            </CardBody>
          </Card>
        </CardBody>
      </Card> */}
    </>
  );
};
