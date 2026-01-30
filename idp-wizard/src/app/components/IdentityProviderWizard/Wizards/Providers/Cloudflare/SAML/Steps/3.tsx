import React, { FC } from "react";
import * as Images from "@app/images/cloudflare/saml";
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

export const CloudflareStepThree: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Click the <b>Copy</b> button next to the <b>SAML Metadata endpoint</b>{" "}
          field, and paste it into the field below. This will load the
          Cloudflare identity provider configuration.
        </div>
      ),
      component: <StepImage src={Images.CloudflareSaml4} alt="Step 3.1" />,
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
      title="Step 3: Upload SAML Metadata"
      instructionList={instructionList}
    />
  );
};
