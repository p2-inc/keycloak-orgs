import React, { FC, useState } from "react";
import {
  InstructionProps,
  MetadataFile,
  Step,
  UrlForm,
} from "@wizardComponents";
import { Card, CardBody, CardTitle } from "@patternfly/react-core";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import { MetadataConfig } from "./forms";

interface Props {
  validateMetadataUrl: ({ url }: { url: string }) => Promise<API_RETURN>;
  url: string;
  uploadMetadataFile: (file: File) => Promise<API_RETURN>;
  uploadCertifcateMetadataInfo: ({
    file,
    ssoUrl,
    entityId,
  }: {
    file: File;
    ssoUrl: METADATA_CONFIG["singleSignOnServiceUrl"];
    entityId: string;
  }) => Promise<API_RETURN>;
  metadata: METADATA_CONFIG | undefined;
}

const forms = {
  METADATA_URL: true,
  METADATA_FILE: true,
  METADATA_CONFIG: true,
};

export const Step2: FC<Props> = ({
  uploadMetadataFile,
  validateMetadataUrl,
  uploadCertifcateMetadataInfo,
  url,
}) => {
  const [formsActive, setFormsActive] = useState(forms);

  const handleMetadatUrlValidation = async ({ url }: { url: string }) => {
    const resp = await validateMetadataUrl({ url });
    if (resp.status === API_STATUS.SUCCESS) {
      setFormsActive({
        ...formsActive,
        METADATA_FILE: false,
        METADATA_CONFIG: false,
      });
    }
    return resp;
  };

  const handleMetadataFileValidation = async ({
    metadataFile,
  }: {
    metadataFile: File;
  }) => {
    const resp = await uploadMetadataFile(metadataFile);
    if (resp.status === API_STATUS.SUCCESS) {
      setFormsActive({
        ...formsActive,
        METADATA_URL: false,
        METADATA_CONFIG: false,
      });
    }
    return resp;
  };

  const handleMetadataConfigValidation = async ({
    ssoUrl,
    entityId,
    metadataFile: certificateFile,
  }: {
    ssoUrl: string;
    entityId: string;
    metadataFile: File;
  }) => {
    const resp = await uploadCertifcateMetadataInfo({
      ssoUrl,
      entityId,
      file: certificateFile,
    });

    if (resp.status === API_STATUS.SUCCESS) {
      setFormsActive({
        ...formsActive,
        METADATA_URL: false,
        METADATA_FILE: false,
      });
    }
    return resp;
  };

  const instructions: InstructionProps[] = [
    {
      component: (
        <div>
          Your identity provider should provide you with configuration
          information in the form of a metadata url, metadata file, or a few
          configuration parameters.
        </div>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Metadata URL</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a SAML metadata URL, input it
              here.
            </div>
            <UrlForm
              handleFormSubmit={handleMetadatUrlValidation}
              formActive={formsActive.METADATA_URL}
              url={url}
            />
          </CardBody>
        </Card>
      ),
    },
    {
      component: (
        <Card>
          <CardTitle>Metadata File</CardTitle>
          <CardBody>
            <div className="pf-u-pb-sm">
              If your identity provider provides a SAML metadata File, upload it
              here.
            </div>
            <MetadataFile
              handleFormSubmit={handleMetadataFileValidation}
              formActive={formsActive.METADATA_FILE}
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
              If your identity provider provides a SAML metadata URL, Entity ID,
              and Certificate, input those below.
            </div>
            <MetadataConfig
              handleFormSubmit={handleMetadataConfigValidation}
              formActive={formsActive.METADATA_CONFIG}
            />
          </CardBody>
        </Card>
      ),
    },
  ];

  return (
    <Step
      title="Step 2: Configure Application Metadata"
      instructionList={instructions}
    />
  );
};
