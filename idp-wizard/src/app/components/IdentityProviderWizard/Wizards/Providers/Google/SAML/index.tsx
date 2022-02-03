import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import GoogleLogo from "@app/images/provider-logos/google-workspace-logo.svg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4, Step5, Step6 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useKeycloak } from "@react-keycloak/web";
import { API_STATUS, METADATA_CONFIG } from "@app/configurations/api-status";
import { generateId } from "@app/utils/generate-id";
import { useNavigateToBasePath } from "@app/routes";

const nanoId = generateId();

export const GoogleWizard: FC = () => {
  const title = "Google wizard";
  const navigateToBasePath = useNavigateToBasePath();

  const [alias, setAlias] = useState(`google-saml-${nanoId}`);
  const [stepIdReached, setStepIdReached] = useState(1);
  const { keycloak } = useKeycloak();
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();

  const identifierURL = `${getServerUrl()}/admin/realms/${getRealm()}/identity-provider/import-config`;
  const acsUrl = `${getServerUrl()}/admin/realms/${getRealm()}/broker/${alias}/endpoint`;
  const entityId = `${getServerUrl()}/realms/${getRealm()}`;

  const [metadata, setMetadata] = useState<METADATA_CONFIG | null>(null);
  const [isFormValid, setIsFormValid] = useState(false);

  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const Axios = axios.create({
    headers: {
      authorization: `bearer ${keycloak.token}`,
    },
  });

  const onNext = (newStep) => {
    if (stepIdReached === 8) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const uploadMetadataFile = async (file: File) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata(resp.data);
        return true;
      }
    } catch (err) {
      console.log(err);
    }

    return false;
  };

  const handleFormSubmit = async ({
    metadataFile: file,
  }: {
    metadataFile: File;
  }) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata(resp.data);
        setIsFormValid(true);

        return {
          status: API_STATUS.SUCCESS,
          message:
            "Configuration successfully validated with PingOne Saml. Continue to next step.",
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message:
        "Configuration validation failed with PingOne Saml. Check file and try again.",
    };
  };

  const createGoogleIdp = async () => {
    setIsValidating(true);
    setResults("Creating SAML IdP...");

    const payload: IdentityProviderRepresentation = {
      alias: alias,
      displayName: "Google SAML Single Sign-on",
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });
      setResults("Google IdP created successfully. Click finish.");
      setStepIdReached(8);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults("Error creating IdP for Google SAML.");
      setError(true);
    } finally {
      setIsValidating(false);
    }
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
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Upload Google IdP Information",
      component: (
        <Step3
          uploadMetadataFile={uploadMetadataFile}
          handleFormSubmit={handleFormSubmit}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: metadata !== null,
    },
    {
      id: 4,
      name: "Enter Service Provider Details",
      component: <Step4 acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Configure Attribute Mapping",
      component: <Step5 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 5,
    },
    {
      id: 6,
      name: "Configure User Access",
      component: <Step6 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 6,
    },
    {
      id: 7,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Google Cloud SAML."
          buttonText="Create SAML IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createGoogleIdp}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 8,
      canJumpTo: stepIdReached >= 7,
    },
  ];

  return (
    <>
      <Header logo={GoogleLogo} />
      <PageSection
        type={PageSectionTypes.wizard}
        variant={PageSectionVariants.light}
      >
        <Wizard
          navAriaLabel={`${title} steps`}
          isNavExpandable
          mainAriaLabel={`${title} content`}
          onClose={closeWizard}
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
