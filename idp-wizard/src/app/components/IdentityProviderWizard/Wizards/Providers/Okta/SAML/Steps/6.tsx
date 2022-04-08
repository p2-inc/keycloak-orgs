import React, { FC } from "react";
import {
  InstructionProps,
  Step,
  StepImage,
  UrlCard,
  UrlForm,
} from "@wizardComponents";
import * as Images from "@app/images/okta/saml";

import { API_RETURN_PROMISE } from "@app/configurations/api-status";

type Props = {
  url: string;
  handleFormSubmit: ({ url }: { url: string }) => API_RETURN_PROMISE;
};

export const Step6: FC<Props> = ({ url, handleFormSubmit }) => {
  const instructions: InstructionProps[] = [
    {
      text: 'In the "Sign On" section, right click and click to copy the "Identity Provider metadata" link and paste below.',
      component: <StepImage src={Images.OktaSaml8} alt="Step 6.1" />,
    },
    {
      component: <div>Enter and validate the Identity Provider metadata.</div>,
    },
    {
      component: (
        <UrlCard>
          <UrlForm
            url={url}
            urlLabel="Provider URL"
            handleFormSubmit={handleFormSubmit}
          />
        </UrlCard>
      ),
    },
  ];

  return (
    <Step
      title="Step 6: Upload Okta IdP Information"
      instructionList={instructions}
    />
  );
};
