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
import { useHistory } from "react-router";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { API_STATUS } from "@app/configurations/api-status";
import { customAlphabet } from "nanoid";
import { alphanumeric } from "nanoid-dictionary";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";

const nanoId = customAlphabet(alphanumeric, 6);

export const Auth0WizardOIDC: FC = () => {
  const [alias, setAlias] = useState(`auth0-oidc-${nanoId()}`);
  const loginRedirectURL = `${process.env.KEYCLOAK_URL}/realms/${process.env.REALM}/broker/${alias}/endpoint`;

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);

  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResults, setValidationResults] = useState<
    Record<string, any> | undefined
  >({});
  const [config, setConfig] = useState({});
  const history = useHistory();
  const [kcAdminClient] = useKeycloakAdminApi();

  const onNext = (newStep) => {
    if (stepIdReached === steps.length + 1) {
      history.push("/");
    }
    setStepIdReached(stepIdReached < newStep.id ? newStep.id : stepIdReached);
  };

  const closeWizard = () => {
    history.push("/");
  };

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
        realm: process.env.REALM || "wizard",
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
    setResults("Creating Auth0 OIDC IdP...");

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Auth0 OIDC Single Sign-on`,
      providerId: "oidc",
      config: validationResults,
    };

    try {
      await kcAdminClient.identityProviders.create({
        ...payload,
        realm: process.env.REALM!,
      });

      setResults("Auth0 OIDC IdP created successfully. Click finish.");
      setStepIdReached(5);
      setError(false);
      setDisableButton(true);
    } catch (e) {
      setResults("Error creating Auth0 OIDC IdP.");
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
          buttonText="Create Auth0 OIDC IdP in Keycloak"
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === 5,
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
