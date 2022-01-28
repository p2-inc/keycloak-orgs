import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import { OneLoginLogo } from "@app/images/onelogin";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Step1, Step2, Step3, Step4 } from "./steps";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { useHistory } from "react-router";
import { useKeycloak } from "@react-keycloak/web";
import { generateId } from "@app/utils/generate-id";
import {
  API_RETURN,
  API_STATUS,
  METADATA_CONFIG,
} from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";

const nanoId = generateId();

export const OneLoginWizard: FC = () => {
  const [alias, setAlias] = useState(`auth0-oidc-${nanoId}`);
  const navigateToBasePath = useNavigateToBasePath();
  const title = "OneLogin wizard";
  const [stepIdReached, setStepIdReached] = useState(1);
  const [kcAdminClient, setKcAdminClientAccessToken, getServerUrl, getRealm ] = useKeycloakAdminApi();
  const { keycloak } = useKeycloak();
  const history = useHistory();

  const acsUrl = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;
  const entityId = `${getServerUrl()}/realms/${getRealm()}`;

  const [issuerUrl, setIssuerUrl] = useState("");
  const [metadata, setMetadata] = useState<METADATA_CONFIG>();
  const [isFormValid, setIsFormValid] = useState(false);

  // Complete
  const [isValidating, setIsValidating] = useState(false);
  const [results, setResults] = useState("");
  const [error, setError] = useState<null | boolean>(null);
  const [disableButton, setDisableButton] = useState(false);

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    navigateToBasePath();
  };

  const handleFormSubmit = async ({
    url,
  }: {
    url: string;
  }): Promise<API_RETURN> => {
    // make call to submit the URL and verify it
    setIssuerUrl(url);

    try {
      const resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: url,
        providerId: "saml",
        realm: getRealm(),
      });

      setMetadata(resp);
      setIsFormValid(true);

      return {
        status: API_STATUS.SUCCESS,
        message:
          "Configuration successfully validated with OneLogin IdP. Continue to next step.",
      };
    } catch (e) {
      return {
        status: API_STATUS.ERROR,
        message:
          "Configuration validation failed with OneLogin IdP. Check URL and try again.",
      };
    }
  };

  const validateFn = async () => {
    setIsValidating(true);
    setDisableButton(false);
    setResults("Creating OneLogin IdP...");

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `OneLogin Single Sign-on`,
      providerId: "saml",
      config: metadata!,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });

      setResults("OneLogin IdP created successfully. Click finish.");
      setStepIdReached(6);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(
        "Error creating OneLogin IdP. Please confirm there is no OneLogin IdP configured already."
      );
      setError(true);
    } finally {
      setIsValidating(false);
    }
  };

  const steps = [
    {
      id: 1,
      name: "Add a SAML Application",
      component: <Step1 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Enter Service Provider Details",
      component: <Step2 acsUrl={acsUrl} entityId={entityId} />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Configure Attribute Mapping",
      component: <Step3 />,
      hideCancelButton: true,
      enableNext: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Upload OneLogin IdP Information",
      component: <Step4 url={issuerUrl} handleFormSubmit={handleFormSubmit} />,
      hideCancelButton: true,
      enableNext: isFormValid,
      canJumpTo: stepIdReached >= 4,
    },
    {
      id: 5,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with OneLogin."
          buttonText="Create OneLogin IdP in Keycloak"
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
      <Header logo={OneLoginLogo} />
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
