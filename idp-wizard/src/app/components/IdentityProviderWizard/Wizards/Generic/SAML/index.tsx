import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import SamlLogo from "@app/images/provider-logos/saml_logo.svg";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import axios from "axios";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { generateId } from "@app/utils/generate-id";

const nanoId = generateId();

export const GenericSAML: FC = () => {
  const title = "Generic SAML wizard";

  const alias = `generic-saml-${nanoId}`;
  const ssoUrl = `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/broker/${alias}/endpoint`;
  const identifierURL = `${process.env.KEYCLOAK_URL}/admin/realms/${process.env.REALM}/identity-provider/import-config`;
  const entityId = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}`;
  const samlMetadata = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/protocol/saml/descriptor`;

  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  // Metadata
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [metadataUrl, setMetadataUrl] = useState("");
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
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
    if (stepIdReached === steps.length + 1) {
      history.push("/");
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    history.push("/");
  };

  const validateFn = async () => {
    // On final validation set stepIdReached to steps.length+1
    setIsValidating(true);
    setDisableButton(false);
    setResults("Creating SAML IdP...");

    const payload: IdentityProviderRepresentation = {
      alias: "generic-saml",
      displayName: `SAML Single Sign-on`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: process.env.REALM!,
      });

      setResults("SAML IdP created successfully. Click finish.");
      setStepIdReached(6);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        "Error creating SAML IdP. Please confirm there is no SAML configured already."
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const uploadMetadataFile = async (file: File) => {
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
            "Configuration successfully validated with SAML. Continue to next step.",
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message:
        "Configuration validation failed with SAML. Check file and try again.",
    };
  };

  // TODO:: finish this one
  const uploadCertifcateMetadataInfo = async ({
    file,
    ssoUrl,
    entityId,
  }: {
    file: File;
    ssoUrl: METADATA_CONFIG["singleSignOnServiceUrl"];
    entityId: string;
  }) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);
    fd.append("ssoUrl", ssoUrl);
    fd.append("entityId", entityId);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setMetadata(resp.data);
        setIsFormValid(true);
        return {
          status: API_STATUS.SUCCESS,
          message:
            "Configuration successfully validated with SAML. Continue to next step.",
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message:
        "Configuration validation failed with SAML. Check file and try again.",
    };
  };

  const validateMetadataUrl = async ({
    metadataUrl,
  }: {
    metadataUrl: string;
  }): Promise<API_RETURN> => {
    // make call to submit the URL and verify it
    setMetadataUrl(metadataUrl);

    try {
      const resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: metadataUrl,
        providerId: "saml",
        realm: process.env.REALM,
      });

      setMetadata(resp);
      setIsFormValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with SAML. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with SAML. Check URL and try again.",
      };
    }
  };

  const steps = [
    {
      id: 1,
      name: "Create a SAML Application",
      component: (
        <Step1
          ssoUrl={ssoUrl}
          entityId={entityId}
          samlMetadata={samlMetadata}
        />
      ),
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Configure Application Metadata",
      component: (
        <Step2
          validateMetadataUrl={validateMetadataUrl}
          metadata={metadata}
          uploadMetadataFile={uploadMetadataFile}
          uploadCertifcateMetadataInfo={uploadCertifcateMetadataInfo}
        />
      ),
      hideCancelButton: true,
      enableNext: isFormValid,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Configure Attribute Mapping",
      component: <Step3 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Configure User Access",
      component: <Step4 />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with SAML."
          buttonText="Create SAML IdP in Keycloak"
          disableButton={disableButton}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={validateFn}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 6,
      canJumpTo: stepIdReached >= 5,
    },
  ];

  return (
    <>
      <Header logo={SamlLogo} />
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
