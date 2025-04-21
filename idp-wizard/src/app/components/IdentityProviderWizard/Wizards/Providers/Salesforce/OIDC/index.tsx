import React, { FC, useEffect, useState } from "react";
import {
  PageSection,
  PageSectionVariants,
  PageSectionTypes,
  Wizard,
} from "@patternfly/react-core";
import * as Steps from "./Steps";
import * as SharedSteps from "../shared/Steps";
import salesforceLogo from "@app/images/salesforce/salesforce-logo.png";
import { WizardConfirmation, Header } from "@wizardComponents";
import { useKeycloakAdminApi } from "@app/hooks/useKeycloakAdminApi";
import { API_STATUS } from "@app/configurations/api-status";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import { useNavigateToBasePath } from "@app/routes";
import { OidcDefaults, Protocols, Providers } from "@app/configurations";
import { Axios, clearAlias, getAlias, CreateIdp } from "@wizardServices";
import { useApi, usePrompt } from "@app/hooks";
import { useGetFeatureFlagsQuery } from "@app/services";

export const SalesforceWizardOIDC: FC = () => {
  const idpCommonName = "Salesforce OIDC IdP";

  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const navigateToBasePath = useNavigateToBasePath();
  const { getRealm } = useKeycloakAdminApi();
  const {
    alias,
    setAlias,
    loginRedirectURL,
    identifierURL,
    createIdPUrl,
    adminLinkOidc: adminLink,
  } = useApi();

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);

  const [isFormValid, setIsFormValid] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResults, setValidationResults] = useState<
    Record<string, any> | undefined
  >({});
  const [config, setConfig] = useState({
    domain: "",
    clientId: "",
    clientSecret: "",
  });

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.SALESFORCE,
      protocol: Protocols.OPEN_ID,
      preface: "salesforce-oidc",
    });
    setAlias(genAlias);
  }, []);

  const finishStep = 5;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep
  );

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.SALESFORCE,
        protocol: Protocols.OPEN_ID,
      });
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
      const payload = {
        fromUrl: trustedDomain,
        providerId: "oidc",
        realm: getRealm(),
      };

      resp = await Axios.post(identifierURL, payload);

      if (resp.status === 200) {
        setIsFormValid(true);
        setValidationResults(resp.data);
        return {
          status: API_STATUS.SUCCESS,
          message: "Domain and credentials validated. Please continue.",
        };
      }
    } catch (e) {
      console.log(e);
    }
    setIsFormValid(false);
    return {
      status: API_STATUS.ERROR,
      message: "Domain and credentials invalid, please check and try again.",
    };
  };

  const createIdP = async () => {
    setIsValidating(true);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: `Salesforce OIDC Single Sign-on`,
      providerId: "oidc",
      hideOnLogin: true,
      config: {
        ...OidcDefaults,
        ...validationResults,
        clientId: config.clientId,
        clientSecret: config.clientSecret,
      },
    };

    try {
      await CreateIdp({ createIdPUrl, payload, featureFlags });
      // TODO emailAsUsername, Mapper?

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
      clearAlias({
        provider: Providers.SALESFORCE,
        protocol: Protocols.OPEN_ID,
      });
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
      name: "Create Connected App",
      component: <SharedSteps.SalesforceStepConnectedApp stepNumber={1} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 1,
    },
    {
      id: 2,
      name: "Configure OAuth Settings",
      component: <Steps.SalesforceStepTwo loginRedirectURL={loginRedirectURL} />,
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 2,
    },
    {
      id: 3,
      name: "Provide Credentials and Domain",
      component: (
        <Steps.SalesforceStepThree
          onFormSubmission={handleFormSubmission}
          values={config}
        />
      ),
      hideCancelButton: true,
      canJumpTo: stepIdReached >= 3,
      enableNext: isFormValid,
    },
    {
      id: 4,
      name: "Confirmation",
      component: (
        <WizardConfirmation
          title="SSO Configuration Complete"
          message="Your users can now sign-in with Salesforce."
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

  const title = "Salesforce wizard";

  return (
    <>
      <Header logo={salesforceLogo} />
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
