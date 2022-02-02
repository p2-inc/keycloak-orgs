import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import { Card, CardBody } from "@patternfly/react-core";
import { BindConfig, Bind } from "./forms";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  handleAdminConfigValidation: (
    bindCredentials: BindConfig
  ) => API_RETURN_PROMISE;
  config: BindConfig;
};

export const Step3: FC<Props> = ({ handleAdminConfigValidation, config }) => {
  const instructions: InstructionProps[] = [
    {
      text: "Enter your LDAP administrator credentials",
      component: (
        <Card>
          <CardBody>
            <Bind
              handleFormSubmit={handleAdminConfigValidation}
              config={config}
            />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step title="Step 3: LDAP Authentication" instructionList={instructions} />
  );
};
