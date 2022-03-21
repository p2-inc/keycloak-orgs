import React, { Dispatch, FC, SetStateAction, useState } from "react";
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
import { TrashIcon } from "@patternfly/react-icons";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { useHostname } from "@app/hooks/useHostname";
import { LdapGroupFilter } from "./forms/groups";
import { API_RETURN } from "@app/configurations";
import { GroupConfig } from "./forms";

interface Props {
  handleGroupSave: ({ groupFilter }: GroupConfig) => API_RETURN;
  config: GroupConfig;
}

export const OktaStepThree: FC<Props> = ({ handleGroupSave, config }) => {
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
      text: `This is an optional step. If you have groups defined in Okta, you will find them in the Directory > Groups section.`,
      component: <StepImage src={image} alt="Step3" />,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            <LdapGroupFilter
              handleFormSubmit={handleGroupSave}
              config={config}
            />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step title="Step 3: Group Mapping" instructionList={instructionList} />
  );
};
