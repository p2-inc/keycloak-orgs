import {
  CreateIdp,
  SamlAttributeMapper,
} from "@app/components/IdentityProviderWizard/Wizards/services";
import { Protocols, Providers, SamlIDPDefaults } from "@app/configurations";
import { API_STATUS, METADATA_CONFIG } from "@app/configurations/api-status";
import { useApi, usePrompt } from "@app/hooks";
import { useGenerateIdpDisplayName } from "@app/hooks/useGenerateIdpDisplayName";
import authoLogo from "@app/images/auth0/auth0-logo.png";
import { useNavigateToBasePath } from "@app/routes";
import { useGetFeatureFlagsQuery } from "@app/services";
import IdentityProviderRepresentation from "@keycloak/keycloak-admin-client/lib/defs/identityProviderRepresentation";
import {
  PageSection,
  PageSectionTypes,
  PageSectionVariants,
  Wizard,
} from "@patternfly/react-core";
import { Header, WizardConfirmation } from "@wizardComponents";
import { Axios, clearAlias, getAlias } from "@wizardServices";
import React, { FC, useEffect, useState } from "react";
import * as SharedSteps from "../shared/Steps";
import * as Steps from "./Steps";
import { useCreateTestIdpLink } from "@app/hooks/useCreateTestIdpLink";

export const Auth0WizardSAML: FC = () => {
  const idpCommonName = "Auth0 SAML Identity Provider";

  const navigateToBasePath = useNavigateToBasePath();
  const { data: featureFlags } = useGetFeatureFlagsQuery();
  const {
    alias,
    setAlias,
    adminLinkSaml: adminLink,
    identifierURL,
    createIdPUrl,
    loginRedirectURL,
  } = useApi();
  const { generateIdpDisplayName } = useGenerateIdpDisplayName();

  useEffect(() => {
    const genAlias = getAlias({
      provider: Providers.AUTH0,
      protocol: Protocols.SAML,
      preface: "auth0-saml",
    });
    setAlias(genAlias);
  }, []);

  const [stepIdReached, setStepIdReached] = useState(1);
  const [results, setResults] = useState("");
  const [error, setError] = useState(false);
  const [disableButton, setDisableButton] = useState(false);
  const [isFormValid, setIsFormValid] = useState(false);

  const [configData, setConfigData] = useState<METADATA_CONFIG | null>(null);
  const [isValidating, setIsValidating] = useState(false);

  const finishStep = 6;

  usePrompt(
    "The wizard is incomplete. Leaving will lose any saved progress. Are you sure?",
    stepIdReached < finishStep,
  );

  const { isValidationPendingForAlias } = useCreateTestIdpLink();
  const [idpTestLink, setIdpTestLink] = useState<string>("");
  const checkPendingValidationStatus = async () => {
    const pendingLink = await isValidationPendingForAlias(alias);
    if (pendingLink) {
      setIdpTestLink(pendingLink);
    }
  };

  const onNext = (newStep) => {
    if (stepIdReached === finishStep) {
      clearAlias({
        provider: Providers.AUTH0,
        protocol: Protocols.SAML,
      });
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
        setConfigData({
          ...SamlIDPDefaults,
          ...resp.data,
        });
        setIsFormValid(true);
        return {
          status: API_STATUS.SUCCESS,
          message: `Configuration successfully validated with ${idpCommonName}. Continue to next step.`,
        };
      }
    } catch (err) {
      console.log(err);
    }

    return {
      status: API_STATUS.ERROR,
      message: `Configuration validation failed with ${idpCommonName}. Check file and try again.`,
    };
  };

  const createIdP = async () => {
    setIsValidating(true);
    setResults(`Creating ${idpCommonName}...`);

    const payload: IdentityProviderRepresentation = {
      alias,
      displayName: generateIdpDisplayName(alias),
      providerId: "saml",
      hideOnLogin: true,
      config: configData!,
    };

    try {
      await CreateIdp({ createIdPUrl, payload, featureFlags });

      await SamlAttributeMapper({
        alias,
        createIdPUrl,
        usernameAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/username",
          friendlyName: "",
        },
        emailAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress",
          friendlyName: "",
        },
        firstNameAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname",
          friendlyName: "",
        },
        lastNameAttribute: {
          attributeName:
            "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname",
          friendlyName: "",
        },
        featureFlags,
      });

      setResults(`${idpCommonName} created successfully. Click finish.`);
      setStepIdReached(finishStep);
      setError(false);
      setDisableButton(true);
      await checkPendingValidationStatus();
      clearAlias({
        provider: Providers.AUTH0,
        protocol: Protocols.SAML,
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
          title="SSO Configuration Complete. Create Identity Provider."
          message="Auth0."
          buttonText={`Create ${idpCommonName}`}
          resultsText={results}
          error={error}
          isValidating={isValidating}
          validationFunction={createIdP}
          disableButton={disableButton}
          adminLink={adminLink}
          adminButtonText={`Manage ${idpCommonName}`}
          idpTestLink={idpTestLink}
        />
      ),
      nextButtonText: "Finish",
      hideCancelButton: true,
      enableNext: stepIdReached === finishStep,
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
