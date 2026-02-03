import React, { FC } from "react";
import {
  FileCard,
  InstructionProps,
  Step,
  StepImage,
  UrlForm,
} from "@wizardComponents";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";
import * as Images from "@app/images/msft_entra_id/saml";

interface Props {
  url: string;
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
}

export const EntraIdStepThree: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: "Copy the App Federation Metadata URL.",
      component: <StepImage src={Images.AzureSaml1a} alt="Step 1a" />,
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
      title="Step 3: Validate Entra ID SAML Metadata URL"
      instructionList={instructions}
    />
  );
};
