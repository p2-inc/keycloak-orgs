import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  UrlForm,
  UrlCard,
} from "@wizardComponents";
import * as Images from "@app/images/aws";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  url: string;
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
};

export const Step2: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'Enter a "Display Name" and an optional "Description". Click the "Copy URL" link next to the "AWS SSO SAML metadata file" URL, and paste it into the field below. This will load the AWS SSO configuration.',
      component: <StepImage src={Images.AWS_SSO_SAML_3} alt="Step 2.1" />,
    },
    {
      component: (
        <UrlCard>
          <UrlForm
            url={url}
            urlLabel="Provider Url"
            handleFormSubmit={handleFormSubmit}
          />
        </UrlCard>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Upload AWS SSO IdP Information"
      instructionList={instructions}
    />
  );
};
