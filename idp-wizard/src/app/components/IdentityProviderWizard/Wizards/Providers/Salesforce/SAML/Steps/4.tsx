import React, { FC } from "react";
import SalesforceSamlStep2Image from "@app/images/salesforce/SAML/salesforce_saml_2.png";
import SalesforceCommonStep3Image from "@app/images/salesforce/COMMON/salesforce-3.png";
import {
  FileCard,
  InstructionProps,
  Step,
  StepImage,
  UrlForm,
} from "@wizardComponents";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

interface Props {
  url: string;
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
}

export const SalesforceStepFour: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          On the next page, click the <b>Manage</b> button to view your app's SAML settings.
        </div>
      ),
      component: <StepImage src={SalesforceCommonStep3Image} alt="Step 4.1" />,
    },
    {
      text: (
        <div>
          Under the <b>SAML Login Information</b> section, copy the <b>Metadata Discovery Endpoint</b> URL{" "}
          and paste it into the field below.
        </div>
      ),
      component: <StepImage src={SalesforceSamlStep2Image} alt="Step 4.2" />,
    },
    {
      component: (
        <FileCard>
          <UrlForm handleFormSubmit={handleFormSubmit} url={url} />
        </FileCard>
      ),
    },
  ];

  return (
    <Step
      title="Step 4: Upload Salesforce IdP Information"
      instructionList={instructions}
    />
  );
};
