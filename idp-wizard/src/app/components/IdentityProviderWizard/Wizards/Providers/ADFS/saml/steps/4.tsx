import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  UrlForm,
  UrlCard,
} from "@wizardComponents";
import * as Images from "@app/images/adfs/saml";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  url: string;
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
};

export const Step4: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: (
        <div>
          In the ADFS management console, navigate to{" "}
          <b>
            AD FS -{">"} Service -{">"} Endpoints
          </b>
          , and note the path from the Metadata section. The Identity provider
          metadata URL will be constructed by combining your server's
          fully-qualified domain name with this path.
        </div>
      ),
      component: <StepImage src={Images.ADFS_SAML_13} alt="Step 4.1" />,
    },
    {
      component: (
        <UrlCard>
          <UrlForm
            url={url}
            urlLabel="Identity Provider Metadata"
            handleFormSubmit={handleFormSubmit}
          />
        </UrlCard>
      ),
    },
  ];

  return (
    <Step title="Step 4: Import ADFS Metadata" instructionList={instructions} />
  );
};
