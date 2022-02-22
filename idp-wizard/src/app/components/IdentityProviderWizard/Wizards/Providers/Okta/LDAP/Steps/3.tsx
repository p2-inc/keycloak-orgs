import React, { FC, useState } from "react";
import {
  Card,
  CardBody,
  Form,
  FormGroup,
  InputGroup,
  TextInput,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import image from "@app/images/okta/okta-3.png";
import { ArrowRightIcon, TrashIcon } from "@patternfly/react-icons";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";

export const OktaStepThree: FC = () => {
  const [groupList, setGroupList] = useState<string[]>([""]);
  const hostname = useHostname();

  const handleInputChange = (value, index) => {
    const list = [...groupList];
    list[index] = value;
    if (index == list.length - 1 && list[index].length > 0) {
      list.push("");
    }

    setGroupList(list);
  };

  const onDelete = (id) => {
    const list = [...groupList];
    const index = list.indexOf(id, 0);
    if (index > -1) {
      list.splice(index, 1);
    }
    setGroupList(list);
  };

  const instructionList: InstructionProps[] = [
    {
      text: `This is an optional step. If you have groups defined in Okta, you will find them in the Directory ${(
        <ArrowRightIcon />
      )} Groups section.`,
      component: <StepImage src={image} alt="Step3" />,
    },
    {
      text: `If you want to limit groups that can access the ${hostname} app, enter those groups below.`,
      component: <></>,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            <Form>
              <FormGroup label="Groups" fieldId="input-form">
                {groupList.map((x, i) => {
                  return (
                    <InputGroup style={{ padding: "2px" }} key={"group-" + i}>
                      <TextInput
                        key={"text-" + i}
                        value={x}
                        name={i.toString()}
                        id={i.toString()}
                        aria-label="Group"
                        onChange={(value) => handleInputChange(value, i)}
                      />
                      <div style={{ padding: "2px", marginLeft: "5px" }}>
                        <TrashIcon onClick={() => onDelete(x)} color="red" />
                      </div>
                    </InputGroup>
                  );
                })}
              </FormGroup>
            </Form>
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step title="Step 3: Group Mapping" instructionList={instructionList} />
  );
};
