import React, { FC } from "react";
import * as Images from "@app/images/cyberark/SAML";
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

export const CyberArkStepTwo: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructionList: InstructionProps[] = [
    {
      text: (
        <div>
          Switch to the <b>Trust</b> tab, click the <b>Copy URL</b> button next
          to the <b>URL</b> field under the <b>Identity Provider Configuration</b>{" "}
          section, and paste it into the field below.
        </div>
      ),
      component: <StepImage src={Images.CyberArkSaml3} alt="Step 2.1" />,
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
