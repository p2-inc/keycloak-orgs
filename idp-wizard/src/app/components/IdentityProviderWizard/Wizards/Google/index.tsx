import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
  Button,
} from "@patternfly/react-core";
import GoogleLogo from "./google_cloud_logo.svg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4, Step5, Step6 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";
import { nanoid } from "nanoid";

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
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient, setKcAdminClientAccessToken] = useKeycloakAdminApi();
  const identifierURL = `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/identity-provider/import-config`;
  const generateAcsUrl = () =>
    `${process.env.KEYCLOAK_URL}/admin/realms/${
      process.env.REALM
    }/broker/${nanoid(6)}/endpoint`;
  const entityId = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}`;
  const [configData, setConfigData] = useState<ConfigData | null>(null);
  const [acsUrl, setAcsUrl] = useState(generateAcsUrl());
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");

  useEffect(() => {
    setKcAdminClientAccessToken();
  }, []);

  const onNext = (newStep) => {
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  // TODO: leverage a service file with API calls
  const uploadMetadataFile = async (file: File) => {
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

  const createGoogleIdp = async () => {
    setIsValidating(true);
    setResults("Creating SAML IdP...");

    setResults("IdP created successfully.");
    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Add Custom SAML Application",
      component: <Step1 />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Enter Details for your Custom App",
      component: <Step2 />,
      hideCancelButton: true,
      // canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Upload Google IdP Information",
      component: <Step3 uploadMetadataFile={uploadMetadataFile} />,
      hideCancelButton: true,
      // canJumpTo: stepIdReached >= 3,
      enableNext: configData !== null,
    },
    {
      id: 4,
      name: "Enter Service Provider Details",
      component: <Step4 acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      // canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Configure Attribute Mapping",
      component: <Step5 />,
      hideCancelButton: true,
      // canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Configure User Access",
      component: <Step6 />,
      hideCancelButton: true,
      // canJumpTo: stepIdReached >= 6,
    },
    {
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Google Cloud SAML."
          buttonText="Create SAML IdP in Keycloak"
          // resultsText={results}
          // error={error}
          // isValidating={isValidating}
          // validationFunction={validateAzureWizard}
        />
      ),
      nextButtonText: "Finish",
      // canJumpTo: stepIdReached >= 7,
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
          nextButtonText="Continue to Next Step"
          steps={steps}
          height="100%"
          width="100%"
          onNext={onNext}
        />
      </PageSection>
    </>
  );
};
