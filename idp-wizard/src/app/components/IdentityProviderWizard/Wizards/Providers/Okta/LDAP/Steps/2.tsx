import React, { FC } from "react";
import { Card, CardBody } from "@patternfly/react-core";
import { InstructionProps, Step } from "@wizardComponents";
import { oktaValidateUsernamePassword } from "@app/services/OktaValidation";
import { Bind, BindConfig } from "./forms";
import { API_RETURN_PROMISE } from "@app/configurations";

interface Props {
  handleAdminConfigValidation: (
    bindCredentials: BindConfig
  ) => API_RETURN_PROMISE;
  config: BindConfig;
}

export const OktaStepTwo: FC<Props> = ({
  handleAdminConfigValidation,
  config,
}) => {
  const instructionList: InstructionProps[] = [
    {
      text: "Enter your LDAP administrator credentials",
      component: <></>,
    },
    {
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
    <Step
      title="Step 2: LDAP Authentication"
      instructionList={instructionList}
    />
  );
};
