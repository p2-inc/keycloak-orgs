import React, { FC, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import * as commonSteps from "../shared/Steps";
import authoLogo from "@app/images/auth0/auth0-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { API_STATUS } from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { Protocols, Providers } from "@app/configurations";
import { getAlias } from "@wizardServices";
import { usePrompt } from "@app/hooks";

export const Auth0WizardOIDC: FC = () => {
  const idpCommonName = "Auth0 OIDC IdP";
  const alias = getAlias({
    provider: Providers.AUTH0,
    protocol: Protocols.OPEN_ID,
    preface: "auth0-oidc",
  });
  const navigateToBasePath = useNavigateToBasePath();
  const [
    kcAdminClient,
    setKcAdminClientAccessToken,
    getServerUrl,
    getRealm,
    getAuthRealm,
  ] = useKeycloakAdminApi();
  const loginRedirectURL = `${getServerUrl()}/realms/${getRealm()}/broker/${alias}/endpoint`;

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const adminLink = `${getServerUrl()}/admin/${getAuthRealm()}/console/#/realms/${getRealm()}/identity-provider-settings/provider/oidc/${alias}`;

  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResults, setValidationResults] = useState<
    Record<string, any> | undefined
  >({});
  const [config, setConfig] = useState({});

  const finishStep = 5;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      navigateToBasePath();
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => navigateToBasePath();

  const handleFormSubmission = async ({
    domain,
    clientId,
    clientSecret,
  }: {
    domain: string;
    clientId: string;
    clientSecret: string;
  }) => {
    const trustedDomain = `https://${domain}/.well-known/openid-configuration`;
    setConfig({ domain, clientId, clientSecret });

    let resp;
    try {
      resp = await kcAdminClient.identityProviders.importFromUrl({
        fromUrl: trustedDomain,
        providerId: "oidc",
        realm: getRealm(),
      });

      setIsFormValid(true);
      setValidationResults(resp);
      return {
        status: API_STATUS.SUCCESS,
        message: "Domain and credentials validated. Please continue.",
      };
    } catch (e) {
      setIsFormValid(false);
      return {
        status: API_STATUS.ERROR,
        message: "Domain and credentials invalid, please check and try again.",
      };
    }
  };

  const createIdP = async () => {
    setIsValidating(true);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Auth0 OIDC Single Sign-on`,
      providerId: "oidc",
      config: validationResults,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: getRealm()!,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults(`Error creating ${idpCommonName}.`);
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
      component: <commonSteps.Auth0StepOne />,
      hideCancelButton: true,
    },
    {
      id: 2,
      name: "Provide Domain And Credentials",
      component: (
        <Steps.Auth0StepTwo
          onFormSubmission={handleFormSubmission}
          values={config}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
      enableNext: isFormValid,
    },
    {
      id: 3,
      name: "Configure Redirect URI",
      component: <Steps.Auth0StepThree loginRedirectURL={loginRedirectURL} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
    },
    {
      id: 4,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Auth0."
          buttonText={`Create ${idpCommonName} in Keycloak`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName} in Keycloak`}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === finishStep,
      canJumpTo: stepIdReached >= 4,
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
