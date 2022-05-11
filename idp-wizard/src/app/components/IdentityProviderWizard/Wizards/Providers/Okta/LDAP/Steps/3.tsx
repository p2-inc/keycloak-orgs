import React, { FC, useState } from "react";
import { Card, CardBody } from "@patternfly/react-core";
import image from "@app/images/okta/okta-3.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { LdapGroupFilter } from "./forms/groups";
import { API_RETURN } from "@app/configurations";
import { GroupConfig } from "./forms";

interface Props {
  handleGroupSave: ({ groupFilter }: GroupConfig) => API_RETURN;
  config: GroupConfig;
}

export const OktaStepThree: FC<Props> = ({ handleGroupSave, config }) => {
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
