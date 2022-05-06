import React, { FC } from "react";
import * as Images from "@app/images/duo/saml";
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

export const DuoStepTwo: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Click the <b>Copy</b> link next to the <b>Metadata URL</b> field, and
          paste it into the field below. This will load the Duo IdP
          configuration.
        </div>
      ),
      component: <StepImage src={Images.DuoSaml1} alt="Step 2.1" />,
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
      title="Step 2: Upload SAML Metadata"
      instructionList={instructionList}
    />
  );
};
