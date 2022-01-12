import React, { FC, useState } from "react";
import { InstructionProps, Step } from "@wizardComponents";
import { Config, ConfigurationFile, OidcConfig, OpenIdUrl } from "./forms";
import { API_RETURN_PROMISE } from "@app/configurations/api-status";
import { Card, CardBody, CardTitle } from "@patternfly/react-core";

interface Props {
  validateUrl: ({ url }: { url: string }) => API_RETURN_PROMISE;
  validateFile: ({ file }: { file: File }) => API_RETURN_PROMISE;
  validateConfig: (config: OidcConfig) => API_RETURN_PROMISE;
}

const forms = {
  URL: true,
  FILE: true,
  CONFIG: true,
};

export const Step2: FC<Props> = ({
  validateUrl,
  validateFile,
  validateConfig,
}) => {
  const [formsActive, setFormsActive] = useState(forms);

  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          Your identity provider should provide you with configuration
          information in the form of a configuration URL. This is sometimes
          called the OpenID Endpoint Configuration URL or the “Well-Known”
          Configuration.
        </div>
      ),
    },
    {
      text: "",
      component: (
        <div>
          Use either the configuration URL, the configuration file, or if you do
          not have either, you can input the values manually.
        </div>
      ),
    },
    {
      text: "",
      component: (
        <Card>
          <CardTitle>Configuration URL</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a OIDC configuration URL, input
              it here.
            </div>
            <OpenIdUrl
              handleFormSubmit={validateUrl}
              formActive={formsActive.URL}
            />
          </CardBody>
        </Card>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Configuration File</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a OIDC configuration File,
              upload it here.
            </div>
            <ConfigurationFile
              handleFormSubmit={validateFile}
              formActive={formsActive.FILE}
            />
          </CardBody>
        </Card>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Configuration Information</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If not configuration URL or File is available, enter values
              manually below.
            </div>
            <Config
              handleFormSubmit={validateConfig}
              formActive={formsActive.CONFIG}
            />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Configure Application Configuration"
      instructionList={instructions}
    />
  );
};
