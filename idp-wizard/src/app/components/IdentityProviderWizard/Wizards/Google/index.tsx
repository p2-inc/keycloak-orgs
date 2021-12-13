import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
  Button,
} from "@patternfly/react-core";
import GoogleLogo from "./google_cloud_logo.svg";
import { Header } from "../components";
import { Step1, Step2, Step3, Step4, Step5, Step6 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";

// Items to confirm in patternfly
// - Forms
// - Form validation
// - Wizard steps

interface ConfigData {
  validateSignature: "false" | "true";
  loginHint: "false" | "true";
  signingCertificate: string;
  enabledFromMetadata: "false" | "true";
  postBindingLogout: "false" | "true";
  postBindingResponse: "false" | "true";
  nameIDPolicyFormat: string;
  postBindingAuthnRequest: "false" | "true";
  singleSignOnServiceUrl: string;
  wantAuthnRequestsSigned: "false" | "true";
  addExtensionsElementWithKeyInfo: "false" | "true";
}

export const GoogleWizard: FC = () => {
  const title = "Google wizard";
  const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
  const identifierURL = `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/identity-provider/import-config`;
  const [configData, setConfigData] = useState<ConfigData | null>(null);

  const uploadMetadataFile = async (file: File) => {
    await setKcAdminClientAccessToken();

    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await axios.post(identifierURL, fd, {
        headers: {
          authorization: `bearer ${kcAdminClient.accessToken}`,
        },
      });
      console.log("[Config Resp]", resp);

      if (resp.status === 200) {
        setConfigData(resp.data);
        return true;
      }
    } catch (err) {
      console.log(err);
    }
    return false;
  };

  const steps = [
    { name: "Add Custom SAML Application", component: <Step1 /> },
    {
      name: "Enter Details for your Custom App",
      component: <Step2 />,
    },
    {
      name: "Upload Google IdP Information",
      component: <Step3 uploadMetadataFile={uploadMetadataFile} />,
    },
    {
      name: "Enter Service Provider Details",
      component: <Step4 />,
    },
    {
      name: "Configure Attribute Mapping",
      component: <Step5 />,
    },
    {
      name: "Configure User Access",
      component: <Step6 />,
    },
    {
      name: "Confirmation",
      component: <p>Review step content</p>,
      nextButtonText: "Finish",
    },
  ];

  return (
    <>
      <Header logo={GoogleLogo} />
      <PageSection
        marginHeight={10}
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        <Wizard
          navAriaLabel={`${title} steps`}
          isNavExpandable
          mainAriaLabel={`${title} content`}
          // onClose={closeWizard}
          nextButtonText="Next"
          steps={steps}
          height="100%"
          width="100%"
          // onNext={onNext}
        />
      </PageSection>
    </>
  );
};
