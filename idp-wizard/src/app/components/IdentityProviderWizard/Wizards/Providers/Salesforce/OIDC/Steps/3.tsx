import { Card, CardBody } from "@patternfly/react-core";
import React, { FC } from "react";
import SalesforceOidcStep2Image from "@app/images/salesforce/oidc/salesforce_oidc_2.png";
import SalesforceOidcStep3Image from "@app/images/salesforce/oidc/salesforce_oidc_3.png";
import SalesforceOidcStep4Image from "@app/images/salesforce/oidc/salesforce_oidc_4.png";
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

export const SalesforceStepThree: FC<Props> = ({ onFormSubmission, values }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          On the next page, press the <b>Manage Consumer Details</b> button under the <b>API (Enable OAuth Settings)</b> section to view your app's credentials.
        </div>
      ),
      component: <StepImage src={SalesforceOidcStep2Image} alt="Step 3.1" />,
    },
    {
      text: (
        <div>
          Copy the <b>Consumer Key</b> and <b>Consumer Secret</b> and paste them into the fields below.
        </div>
      ),
      component: <StepImage src={SalesforceOidcStep3Image} alt="Step 3.2" />,
    },
    {
      text: (
        <div>
          In the sidebar menu, click <b>My Domain</b> under <b>Company Settings</b> to view your domain.{" "}
          Copy the domain listed under <b>Current My Domain URL</b> and paste it into the domain field below.
        </div>
      ),
      component: <StepImage src={SalesforceOidcStep4Image} alt="Step 3.3" />,
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
      title="Step 3: Provide Credentials and Domain"
      instructionList={instructions}
    />
  );
};
