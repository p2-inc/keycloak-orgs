import { Card, CardBody } from "@patternfly/react-core";
import React, { FC } from "react";
import Auth0Step3Image from "@app/images/auth0/OIDC/auth0-3.png";
import { InstructionProps, Step, StepImage } from "@wizardComponents";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";
import { ClientCredentials } from "../forms";

interface Props {
  onFormSubmission: ({
    domain,
    clientId,
    clientSecret,
  }: {
    domain: string;
    clientId: string;
    clientSecret: string;
  }) => API_RETURN_PROMISE;
  values: {
    domain: string;
    clientId: string;
    clientSecret: string;
  };
}

export const Auth0StepTwo: FC<Props> = ({ onFormSubmission, values }) => {
  const instructions: InstructionProps[] = [
    {
      component: <StepImage src={Auth0Step3Image} alt="Step 2.1" />,
    },
    {
      component: (
        <Card className="card-shadow">
          <CardBody>
            <ClientCredentials
              credentials={values}
              handleFormSubmit={onFormSubmission}
            />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Provide Domain And Credentials"
      instructionList={instructions}
    />
  );
};
