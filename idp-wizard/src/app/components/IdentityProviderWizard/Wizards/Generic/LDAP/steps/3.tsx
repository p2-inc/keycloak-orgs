import React, { FC } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import { Card, CardBody } from "@patternfly/react-core";
import { AdminCredentials, AdminCrednetialsConfig } from "./forms";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  handleAdminConfigValidation: (
    adminCredentials: AdminCrednetialsConfig
  ) => API_RETURN_PROMISE;
};

export const Step3: FC<Props> = ({ handleAdminConfigValidation }) => {
  const instructions: InstructionProps[] = [
    {
      text: "Enter your LDAP administrator credentials",
      component: (
        <Card>
          <CardBody>
            <AdminCredentials handleFormSubmit={handleAdminConfigValidation} />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step title="Step 3: LDAP Authentication" instructionList={instructions} />
  );
};
