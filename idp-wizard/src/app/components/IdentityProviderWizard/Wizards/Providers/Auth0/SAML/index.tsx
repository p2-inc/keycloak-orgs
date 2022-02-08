import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import { API_STATUS, METADATA_CONFIG } from "@app/configurations/api-status";
import { Axios } from "@wizardServices";
import * as Steps from "./Steps";
import * as SharedSteps from "../shared/Steps";
import authoLogo from "@app/images/auth0/auth0-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { customAlphabet } from "nanoid";
import { alphanumeric } from "nanoid-dictionary";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";

const nanoId = customAlphabet(alphanumeric, 6);

export const Auth0WizardSAML: FC = () => {
  const navigateToBasePath = useNavigateToBasePath();
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm] =
    useKeycloakAdminApi();
  const [alias, setAlias] = useState(`auth0-saml-${nanoId()}`);
  const loginRedirectURL = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const identifierURL = `${getServerUrl()}/admin/realms/${getRealm()}/identity-provider/import-config`;

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);

  const [configData, setConfigData] = useState<METADATA_CONFIG | null>(null);
  const [isValidating, setIsValidating] = useState(false);

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => navigateToBasePath();

  const uploadMetadataFile = async (file: File) => {
    const fd = new FormData();
    fd.append("providerId", "saml");
    fd.append("file", file);

    try {
      const resp = await Axios.post(identifierURL, fd);

      if (resp.status === 200) {
        setConfigData(resp.data);
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
  const createIdP = async () => {
    setIsValidating(true);
    setResults("Creating Auth0 SAML IdP...");

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Auth0 SAML Single Sign-on`,
      providerId: "saml",
      config: configData!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });

      setResults("Auth0 SAML IdP created successfully. Click finish.");
      setStepIdReached(6);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults("Error creating Auth0 SAML IdP.");
      setError(true);
    } finally {
      setIsValidating(false);
    }

    setIsValidating(false);
  };

  const steps = [
    {
      id: 1,
      name: "Create An Application",
      component: <SharedSteps.Auth0StepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Select SAML Addon",
      component: <Steps.Auth0StepTwo />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Upload Auth0 IdP Information",
      component: (
        <Steps.Auth0StepThree uploadMetadataFile={uploadMetadataFile} />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: isFormValid,
    },
    {
      id: 4,
      name: "Enter Application Callback URL",
      component: <Steps.Auth0StepFour loginRedirectURL={loginRedirectURL} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Auth0."
          buttonText="Create Auth0 SAML IdP in Keycloak"
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 6,
      canJumpTo: stepIdReached >= 5,
    },
  ];

  const title = "Auth0 wizard";

  return (
    <>
      <Header logo={authoLogo} />
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
